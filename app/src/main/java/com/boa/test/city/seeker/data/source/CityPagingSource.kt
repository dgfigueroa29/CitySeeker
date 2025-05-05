package com.boa.test.city.seeker.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.boa.test.city.seeker.domain.model.CityModel
import timber.log.Timber

class CityPagingSource(
    private val cities: List<CityModel>
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
            val cities = cities
            LoadResult.Page(
                data = cities,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (cities.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Timber.e("Error loading cities: ${e.stackTraceToString()}")
            LoadResult.Error(e)
        }
    }
}
