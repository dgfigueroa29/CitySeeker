package com.boa.test.city.seeker.data.source

import android.content.Context
import com.boa.test.city.seeker.common.FILE_CITY
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.data.network.CityApi
import com.google.gson.stream.JsonReader
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CityDataSourceImpl @Inject constructor(
    private val context: Context,
    private val cityDatabase: CityDatabase,
    private val cityApi: CityApi
) : CityDataSource {
    override suspend fun getAllCities(): List<CityEntity> {
        var cities = cityDatabase.cityDao().getAll()
        val response = cityApi.getAllCities()

        return try {
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    body.use {
                        try {
                            val tempFile = File(context.cacheDir, "temp_$FILE_CITY")
                            it.byteStream().use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            processFile(tempFile)
                            cityDatabase.cityDao().getAll()
                        } catch (e: Exception) {
                            Timber.e("Error processing cities: ${e.stackTraceToString()}")
                            cities
                        }
                    }
                } ?: cityDatabase.cityDao().getAll()
            } else {
                cities
            }
        } catch (e: Exception) {
            Timber.e("Error downloading cities: ${e.stackTraceToString()}")
            cities
        }
    }

    override suspend fun searchCities(query: String): List<CityEntity> =
        cityDatabase.cityDao().searchCities(query)

    private suspend fun processFile(file: File) {
        val reader = JsonReader(file.reader())

        reader.use {
            reader.beginArray()
            val batch = mutableListOf<CityEntity>()
            while (reader.hasNext()) {
                val city = parseCity(reader)
                batch.add(city)
                if (batch.size >= 10000) {
                    insertBatch(batch)
                    batch.clear()
                }
            }

            insertBatch(batch)
            reader.endArray()
        }
    }

    private fun parseCity(reader: JsonReader): CityEntity {
        var id = 0L
        var name = ""
        var country = ""
        var latitude = 0.0
        var longitude = 0.0

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "_id" -> id = reader.nextLong()
                "name" -> name = reader.nextString()
                "country" -> country = reader.nextString()
                "coord" -> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "lon" -> longitude = reader.nextDouble()
                            "lat" -> latitude = reader.nextDouble()
                        }
                    }
                    reader.endObject()
                }

                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return CityEntity(
            id = id,
            name = name,
            country = country,
            latitude = latitude,
            longitude = longitude
        )
    }

    private suspend fun insertBatch(batch: List<CityEntity>) {
        cityDatabase.cityDao().insertAll(batch)
    }
}