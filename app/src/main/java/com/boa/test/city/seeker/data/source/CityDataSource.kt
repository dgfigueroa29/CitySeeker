package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.Flow

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
     * @param cityMapper The mapper to convert entities to models.
     * @param favoriteIds The set of favorite city IDs to mark favorites.
     * @return A [CityPagingSource] that queries Room with proper pagination.
     */
    fun pagingSource(
        query: String,
        cityMapper: CityMapper,
        favoriteIds: Set<String>,
    ): CityPagingSource

    /**
     * Retrieves a city from the data source by its unique ID as a reactive Flow.
     *
     * @param id The ID of the city to retrieve.
     * @return A [Flow] emitting the [CityEntity] with the specified ID, or `null` if not found.
     * The Flow re-emits whenever the underlying row changes, enabling offline-first reactivity.
     */
    fun getCityById(id: Long): Flow<CityEntity?>

    /**
     * Maps cities based on a query.
     *
     * This function retrieves cities from the database and maps them to [CityModel] objects.
     *
     * @param query The search query string used to find matching cities.
     * @return A list of [CityModel] objects that match the query.
     */
    suspend fun mapCities(query: String): List<CityModel>

    fun getDistinctCountries(): Flow<List<String>>

    fun getCitiesByCountry(country: String): Flow<List<CityEntity>>
}
