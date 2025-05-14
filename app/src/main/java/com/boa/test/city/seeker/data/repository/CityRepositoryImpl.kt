package com.boa.test.city.seeker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.data.source.CityTrie
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

/**
 * Implementation of the [CityRepository] interface.
 *
 * This class provides concrete implementations for fetching and searching city data.
 * It utilizes a [CityDataSource] to retrieve raw city data and a [CityTrie] for efficient prefix searching.
 *
 * @property cityDataSource The data source for retrieving city data.
 */
class CityRepositoryImpl(
    private val cityDataSource: CityDataSource
) : CityRepository {
    private val trie = CityTrie()
    private var isInitialized = false

    /**
     * Initializes the Trie data structure with all cities from the data source.
     * This function is idempotent; it will only initialize the Trie once.
     */
    suspend fun initializeTrie() {
        try {
            if (isInitialized) return
            val cities = CityMapper().mapAll(cityDataSource.getAllCities())
            cities.forEach {
                trie.insert(it)
            }

            isInitialized = true
        } catch (e: Exception) {
            Timber.e("Error initializeTrie: ${e.stackTraceToString()}")
        }
    }

    /**
     * Searches for cities based on the provided query.
     *
     * This function utilizes a PagingSource to efficiently load city data in chunks.
     * If the query is blank, it triggers the initialization of the Trie data structure,
     * which is used for prefix-based searching.
     *
     * @param query The search query string.
     * @return A [Flow] of [PagingData] containing [CityModel] objects that match the query.
     * Returns an empty PagingData flow in case of an error.
     */
    override suspend fun searchCities(query: String): Flow<PagingData<CityModel>> {
        try {
            val pagingSource =
                cityDataSource.pagingSource(query, trie)
            if (query.isBlank()) {
                initializeTrie()
            }
            return Pager(
                config = PagingConfig(pageSize = LIMIT),
                pagingSourceFactory = { pagingSource }).flow

        } catch (e: Exception) {
            Timber.e("Error initializeTrie: ${e.stackTraceToString()}")
            return flow { PagingData.Companion.empty<CityModel>() }
        }
    }

    /**
     * Retrieves a city by its unique identifier.
     *
     * @param id The unique identifier of the city.
     * @return A [Flow] emitting a [CityModel] corresponding to the given ID.
     * If no city is found or an error occurs, an empty [CityModel] is emitted.
     */
    override suspend fun getCityById(id: Long): Flow<CityModel> {
        return flow {
            try {
                val city = cityDataSource.getCityById(id)
                if (city != null) {
                    emit(CityMapper().map(city))
                } else {
                    emit(CityModel())
                }
            } catch (e: Exception) {
                Timber.e("Error getCityById: ${e.stackTraceToString()}")
                emit(CityModel())
            }
        }
    }
}
