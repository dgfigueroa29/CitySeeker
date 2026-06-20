package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.domain.util.removeSpecialCharacters
import com.google.gson.stream.JsonReader
import timber.log.Timber
import java.io.File

class CityFileProcessor(
    private val cityDatabase: CityDatabase,
) {
    suspend fun processFile(file: File) {
        try {
            if (file.isFile) {
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
                            if (batch.size >= LIMIT) {
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
            }
        } catch (e: Exception) {
            Timber.e("Error readFile: ${e.stackTraceToString()}")
        }
    }

    @Suppress("NestedBlockDepth")
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
            longitude = longitude,
        )
    }

    private suspend fun insertBatch(batch: List<CityEntity>) {
        cityDatabase.cityDao().insertAll(batch)
    }
}
