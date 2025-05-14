package com.boa.test.city.seeker.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.boa.test.city.seeker.domain.model.CityModel
import timber.log.Timber

/**
 * A [PagingSource] implementation for loading a predefined list of [CityModel] objects.
 *
 * This PagingSource is designed to serve as an in-memory source for paging, providing data
 * from a fixed list of cities. It's suitable for scenarios where the entire dataset
 * is available upfront and does not require network or database operations for subsequent pages.
 *
 * The paging is simulated using page numbers (Int) as keys. However, since the entire list
 * is provided at once in the `load` method, the concept of distinct pages loaded sequentially
 * is somewhat simplified in this implementation. The `nextKey` calculation will always suggest
 * the next page, but the `load` method will still return the entire list regardless of the key.
 *
 * @property cities The predefined list of [CityModel] objects to be paged.
 */
class CityPagingSource(
    private val cities: List<CityModel>
) :
    PagingSource<Int, CityModel>() {
    /**
     * Calculates the refresh key for the PagingSource.
     *
     * This method is called when the PagingData needs to be refreshed. It helps determine the
     * key to start loading from after a refresh.
     *
     * @param state The current PagingState, which provides information about the loaded pages
     * and the current scroll position.
     * @return The refresh key (typically an index) or `null` if no valid refresh key can be determined.
     */
    override fun getRefreshKey(state: PagingState<Int, CityModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    /**
     * Loads a page of data from the predefined list of cities.
     *
     * This method is called by the Paging library to retrieve data for a specific page.
     * It returns a [LoadResult.Page] containing the loaded data and keys for the previous and next pages.
     *
     * @param params The [LoadParams] containing information about the page to load,
     * such as the key (page number) and load size.
     * @return A [LoadResult] indicating the success or failure of the load operation.
     *         On success, it returns a [LoadResult.Page] with the data, previous key, and next key.
     *         On failure, it returns a [LoadResult.Error] with the exception.
     */
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
