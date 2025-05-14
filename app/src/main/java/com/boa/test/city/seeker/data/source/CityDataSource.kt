package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.entity.CityEntity

/**
 * Defines the interface for accessing city data from various sources.
 *
 * Implementations of this interface are responsible for providing city data,
 * such as retrieving all cities, searching for cities, or providing a paging source for city data.
 */
interface CityDataSource {
    /**
     * Retrieves all cities from the data source.
     *
     * @return A list of [CityEntity] objects representing all cities.
     */
    suspend fun getAllCities(): List<CityEntity>

    /**
     * Searches for cities matching the given query.
     *
     * @param query The search query string.
     * @return A list of [CityEntity] objects that match the query.
     */
    suspend fun searchCities(query: String): List<CityEntity>

    /**
     * Returns a [CityPagingSource] for the given query.
     *
     * @param query The query to search for.
     * @param trie The CityTrie to use for searching.
     * @return A [CityPagingSource] that can be used to load city data in pages.
     */
    suspend fun pagingSource(query: String, trie: CityTrie): CityPagingSource

    /**
     * Retrieves a city from the data source by its unique ID.
     *
     * @param id The ID of the city to retrieve.
     * @return The [CityEntity] with the specified ID, or `null` if no city is found with that ID.
     */
    suspend fun getCityById(id: Long): CityEntity?
}
