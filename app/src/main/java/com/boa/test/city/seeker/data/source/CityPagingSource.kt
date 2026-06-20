package com.boa.test.city.seeker.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.boa.test.city.seeker.data.local.dao.CityDao
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.domain.model.CityModel
import timber.log.Timber

class CityPagingSource(
    private val cityDao: CityDao,
    private val query: String,
    private val cityMapper: CityMapper,
    private val favoriteIds: Set<String>,
    private val pageSize: Int = PAGE_SIZE,
) : PagingSource<Int, CityModel>() {
    companion object {
        const val PAGE_SIZE = 50
        const val INITIAL_PAGE = 0
    }

    override fun getRefreshKey(state: PagingState<Int, CityModel>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CityModel> =
        try {
            val page = params.key ?: INITIAL_PAGE
            val offset = page * params.loadSize
            val entities = if (query.isBlank()) {
                cityDao.getAllPaginated(params.loadSize, offset)
            } else {
                cityDao.searchCitiesPaginated(query, params.loadSize, offset)
            }
            val models = entities.map { entity ->
                cityMapper.map(entity).copy(
                    isFavorite = favoriteIds.contains(entity.id.toString()),
                )
            }
            LoadResult.Page(
                data = models,
                prevKey = if (page == INITIAL_PAGE) null else page - 1,
                nextKey = if (entities.size < params.loadSize) null else page + 1,
            )
        } catch (e: Exception) {
            Timber.e("Error loading cities page: ${e.stackTraceToString()}")
            LoadResult.Error(e)
        }
}
