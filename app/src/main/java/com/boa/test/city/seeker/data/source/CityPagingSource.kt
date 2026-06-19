package com.boa.test.city.seeker.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.boa.test.city.seeker.domain.model.CityModel
import timber.log.Timber

/**
 * A [PagingSource] implementation for loading [CityModel] objects with proper pagination.
 *
 * This PagingSource efficiently handles large datasets (200k+ cities) by loading data
 * in pages rather than all at once. Each page contains a configurable number of items.
 *
 * @property cities The complete list of [CityModel] objects to be paged.
 * @property pageSize The number of items to load per page. Default is 50.
 */
class CityPagingSource(
    private val cities: List<CityModel>,
    private val pageSize: Int = PAGE_SIZE,
) : PagingSource<Int, CityModel>() {
    companion object {
        const val PAGE_SIZE = 50
        const val INITIAL_PAGE = 0
    }

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
    override fun getRefreshKey(state: PagingState<Int, CityModel>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
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
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CityModel> =
        try {
            val page = params.key ?: INITIAL_PAGE
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, cities.size)

            if (startIndex >= cities.size) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = null,
                )
            } else {
                LoadResult.Page(
                    data = cities.subList(startIndex, endIndex),
                    prevKey = if (page == INITIAL_PAGE) null else page - 1,
                    nextKey = if (endIndex >= cities.size) null else page + 1,
                )
            }
        } catch (e: Exception) {
            Timber.e("Error loading cities: ${e.stackTraceToString()}")
            LoadResult.Error(e)
        }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The total number of cities.
     */
    fun getItemCount(): Int = cities.size
}
