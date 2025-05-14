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
    suspend fun searchCities(query: String): Flow<PagingData<CityModel>>

    /**
     * Retrieves a city by its ID.
     *
     * @param id The unique identifier of the city.
     * @return A [Flow] emitting the [CityModel] matching the given ID.
     */
    suspend fun getCityById(id: Long): Flow<CityModel>
}
