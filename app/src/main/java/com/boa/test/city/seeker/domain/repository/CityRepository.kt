package com.boa.test.city.seeker.domain.repository

import androidx.paging.PagingData
import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing city data.
 */
interface CityRepository {
    /**
     * Searches for cities based on a given query.
     *
     * This function is expected to return a [Flow] of [PagingData] containing [CityModel] objects
     * that match the provided query. The search operation should be asynchronous and suitable
     * for use with pagination.
     *
     * @param query The search query string.
     * @return A [Flow] of [PagingData] containing [CityModel] objects that match the query.
     */
    suspend fun searchCitiesAndPaginate(query: String): Flow<PagingData<CityModel>>

    /**
     * Searches for cities based on a given query, with an option to filter by favorites.
     *
     * This function performs a search for cities whose names match or contain the provided query.
     * It can optionally filter the results to include only cities that have been marked as favorites.
     * It is a suspended function, meaning it can be called from a coroutine.
     *
     * @param query The search query string.
     * @param withOnlyFavorites A boolean indicating whether to filter the results to include only favorite cities.
     * @return A list of [CityModel] objects that match the query and the favorite filter.
     */
    suspend fun searchCities(query: String, withOnlyFavorites: Boolean): List<CityModel>

    /**
     * Retrieves a city by its ID.
     *
     * @param id The unique identifier of the city.
     * @return A [Flow] emitting the [CityModel] matching the given ID.
     */
    fun getCityById(id: Long): Flow<CityModel>
}
