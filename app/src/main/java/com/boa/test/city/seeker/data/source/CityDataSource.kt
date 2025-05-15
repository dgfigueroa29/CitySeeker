package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.domain.model.CityModel

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

    /**
     * Maps cities based on a query using a trie data structure.
     *
     * This function is typically used for suggesting cities based on a partial
     * or full query string by leveraging the efficient searching capabilities
     * of a trie.
     *
     * @param query The search query string used to find matching cities.
     * @param trie The [CityTrie] data structure used for efficient city lookups.
     * @return A list of [CityModel] objects that match the query, potentially
     *         representing suggestions or exact matches.
     */
    suspend fun mapCities(query: String, trie: CityTrie): List<CityModel>
}
