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
import timber.log.Timber

class CityRepositoryImpl(
    private val cityDataSource: CityDataSource
) : CityRepository {
    private val trie = CityTrie()
    private var isInitialized = false

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

    override suspend fun searchCities(query: String): Flow<PagingData<CityModel>> {
        val pagingSource =
            cityDataSource.pagingSource(query, trie)
        if (query.isBlank()) {
            initializeTrie()
        }
        return Pager(
            config = PagingConfig(pageSize = LIMIT, prefetchDistance = 2),
            pagingSourceFactory = { pagingSource }).flow
    }
}
