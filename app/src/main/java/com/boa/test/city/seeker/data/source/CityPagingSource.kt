package com.boa.test.city.seeker.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.domain.model.CityModel

class CityPagingSource(
    private val cityDataSource: CityDataSource,
    private val query: String,
    private val trie: CityTrie
) :
    PagingSource<Int, CityModel>() {
    override fun getRefreshKey(state: PagingState<Int, CityModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CityModel> {
        return try {
            val page = params.key ?: 1
            val cities = provideCities(query, trie)
            LoadResult.Page(
                data = cities,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (cities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private suspend fun provideCities(query: String, trie: CityTrie): List<CityModel> {
        var cities = trie.search(query)
        cities = if (query.isBlank() && cities.isEmpty()) {
            CityMapper().mapAll(cityDataSource.getAllCities())
        } else {
            CityMapper().mapAll(cityDataSource.searchCities(query))
        }

        return cities
    }
}