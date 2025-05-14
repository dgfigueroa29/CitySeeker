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

/**
 * Implementation of the [CityDataSource] interface.
 *
 * This class is responsible for providing city data from various sources,
 * including a local database, a cached file, and a network API.
 * It handles caching and processing of city data.
 *
 * @property context The application context, used for accessing cache directory.
 * @property cityDatabase The local Room database for storing city data.
 * @property cityApi The Retrofit API service for fetching city data from the network.
 */
class CityDataSourceImpl @Inject constructor(
    private val context: Context,
    private val cityDatabase: CityDatabase,
    private val cityApi: CityApi
) : CityDataSource {
    /**
     * Retrieves all cities.
     *
     * This function first attempts to load cities from the local database.
     * If the database is empty, it checks for a cached city file.
     * If no cached file exists, it attempts to download the cities from the network API,
     * caches the downloaded data, processes it, and then returns the cities from the database.
     * If a cached file exists and the database is empty, it processes the cached file and returns
     * the cities from the database.
     *
     * @return A list of [CityEntity] objects representing all cities.
     */
    @Suppress("NestedBlockDepth")
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

    /**
     * Searches for cities based on the provided query string.
     *
     * If the query is not empty, it searches the local database for cities matching the query.
     * If the query is empty, it returns all cities from the local database.
     * The results are returned as a distinct list of [CityEntity].
     *
     * @param query The search query string.
     * @return A list of distinct [CityEntity] objects matching the search criteria.
     */
    override suspend fun searchCities(query: String): List<CityEntity> {
        val cities = (if (query.isNotEmpty()) {
            cityDatabase.cityDao().searchCities(query)
        } else {
            cityDatabase.cityDao().getAll()
        }).distinct()
        return cities
    }

    /**
     * Creates a [CityPagingSource] for the given query.
     *
     * This function first attempts to search the provided [Trie] for cities matching the query.
     * If the query is blank and the trie search returns no results, it fetches all cities.
     * Otherwise, it performs a database search for cities matching the query.
     * The results are then mapped to the domain model and used to create the [CityPagingSource].
     *
     * @param query The search query.
     * @param trie The [Trie] to use for initial searching.
     * @return A [CityPagingSource] containing the search results.
     * @throws Exception If an error occurs during the process.
     */
    override suspend fun pagingSource(query: String, trie: CityTrie): CityPagingSource {
        try {
            var cities = trie.search(query)
            cities = (if (query.isBlank() && cities.isEmpty()) {
                CityMapper().mapAll(getAllCities())
            } else {
                CityMapper().mapAll(searchCities(query))
            }).distinct()

            return CityPagingSource(cities)
        } catch (e: Exception) {
            Timber.e("Error creating paging source: ${e.stackTraceToString()}")
            return CityPagingSource(emptyList())
        }
    }

    /**
     * Retrieves a city entity from the database by its ID.
     *
     * @param id The unique identifier of the city.
     * @return The [CityEntity] if found, or `null` if an error occurs or the city is not found.
     */
    override suspend fun getCityById(id: Long): CityEntity? {
        return try {
            cityDatabase.cityDao().getCityById(id)
        } catch (e: Exception) {
            Timber.e("Error getting by id: ${e.stackTraceToString()}")
            null
        }
    }

    /**
     * Processes a JSON file containing city data and inserts it into the database in batches.
     *
     * Reads city data from the provided file using a [JsonReader], parses each city object,
     * and adds valid cities (with non-blank names) to a batch. When the batch reaches a
     * certain size (10000), it is inserted into the database, and the batch is cleared.
     * Any remaining cities in the batch are inserted after processing the entire file.
     *
     * Error handling is included for file reading and JSON parsing.
     *
     * @param file The [File] object representing the JSON file to be processed.
     */
    @Suppress("NestedBlockDepth")
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

    /**
     * Parses a JSON object representing a city from the given JsonReader.
     *
     * @param reader The JsonReader to read the city data from.
     * @return A [CityEntity] parsed from the JSON data.
     */
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
            longitude = longitude
        )
    }

    /**
     * Inserts a batch of [CityEntity] objects into the database.
     *
     * @param batch The list of [CityEntity] objects to insert.
     */
    private suspend fun insertBatch(batch: List<CityEntity>) {
        cityDatabase.cityDao().insertAll(batch)
    }
}
