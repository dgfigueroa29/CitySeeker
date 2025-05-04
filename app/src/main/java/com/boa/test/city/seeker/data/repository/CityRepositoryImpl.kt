package com.boa.test.city.seeker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.data.source.CityPagingSource
import com.boa.test.city.seeker.data.source.CityTrie
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow

class CityRepositoryImpl(
    private val cityDataSource: CityDataSource
) : CityRepository {
    private val trie = CityTrie()
    private var isInitialized = false

    suspend fun initializeTrie() {
        if (isInitialized) return
        val cities = CityMapper().mapAll(cityDataSource.getAllCities())
        cities.forEach {
            trie.insert(it)
        }

        isInitialized = true
    }

    override suspend fun searchCities(query: String): Flow<PagingData<CityModel>> {
        if (query.isBlank()) {
            initializeTrie()
        }
        //Detecting available source of data and prepare it for use
        return Pager(
            config = PagingConfig(pageSize = 100, prefetchDistance = 2),
            pagingSourceFactory = { CityPagingSource(cityDataSource, query, trie) }).flow
    }
}
