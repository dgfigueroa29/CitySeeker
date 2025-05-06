package com.boa.test.city.seeker.data.source

import android.content.Context
import com.boa.test.city.seeker.common.FILE_CITY
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.network.CityApi
import com.boa.test.city.seeker.domain.util.removeSpecialCharacters
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
        val cacheDir = context.cacheDir
        var tempFile = File(cacheDir, FILE_CITY)
        var needDownload = false

        if (!tempFile.exists()) {
            tempFile = File(cacheDir, "temp_$FILE_CITY")
            if (!tempFile.exists()) {
                tempFile.createNewFile()
                needDownload = true
            }
        }

        return try {
            if (needDownload && cities.isEmpty()) {
                val response = cityApi.getAllCities()
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        body.use {
                            try {
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
                    } ?: cities
                } else {
                    cities
                }
            } else {
                if (cities.isEmpty()) {
                    processFile(tempFile)
                    cityDatabase.cityDao().getAll()
                } else {
                    cities
                }
            }
        } catch (e: Exception) {
            Timber.e("Error downloading cities: ${e.stackTraceToString()}")
            cities
        }
    }

    override suspend fun searchCities(query: String): List<CityEntity> {
        val cities = cityDatabase.cityDao().searchCities(query).distinct()
        return cities
    }

    override suspend fun pagingSource(query: String, trie: CityTrie): CityPagingSource {
        var cities = trie.search(query)
        cities = if (query.isBlank() && cities.isEmpty()) {
            CityMapper().mapAll(getAllCities())
        } else {
            CityMapper().mapAll(searchCities(query))
        }

        return CityPagingSource(cities)
    }

    private suspend fun processFile(file: File) {
        try {
            val reader = JsonReader(file.reader())

            reader.use {
                try {
                    reader.beginArray()
                    val batch = mutableListOf<CityEntity>()
                    while (reader.hasNext()) {
                        val city = parseCity(reader)
                        if (city.name.isNotBlank()) {
                            batch.add(city)
                        }
                        if (batch.size >= 10000) {
                            insertBatch(batch)
                            batch.clear()
                        }
                    }

                    insertBatch(batch)
                    reader.endArray()
                } catch (e: Exception) {
                    Timber.e("Error processFile: ${e.stackTraceToString()}")
                }
            }
        } catch (e: Exception) {
            Timber.e("Error readFile: ${e.stackTraceToString()}")
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
                "name" -> name = reader.nextString().removeSpecialCharacters()
                "country" -> country = reader.nextString().removeSpecialCharacters()
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
