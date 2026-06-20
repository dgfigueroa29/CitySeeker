package com.boa.test.city.seeker.presentation.feature.city.list

import app.cash.turbine.test
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.PerformanceMonitor
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.usecase.RecordSearchUseCase
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {
    private lateinit var viewModel: ListViewModel
    private lateinit var searchCityUseCase: SearchCityUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var recordSearchUseCase: RecordSearchUseCase
    private val analyticsService: AnalyticsService = mockk(relaxed = true)
    private val performanceMonitor: PerformanceMonitor = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCities =
        listOf(
            CityModel(1, "Denver", "US", 39.7392, -104.9903),
            CityModel(2, "Dallas", "US", 32.7767, -96.7970),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchCityUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        recordSearchUseCase = mockk(relaxed = true)
        viewModel =
            ListViewModel(
                searchCityUseCase,
                toggleFavoriteUseCase,
                recordSearchUseCase,
                analyticsService,
                performanceMonitor,
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshQuery should update query state`() {
        // Given
        coEvery {
            searchCityUseCase(any(), any())
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshQuery("Denver")

        // Then
        assertEquals("Denver", viewModel.listState.queryState.value)
    }

    @Test
    fun `refreshQuery should update city list`() {
        // Given
        coEvery {
            searchCityUseCase("Denver", false)
        } returns
            flow {
                emit(UiStateModel.Loading(true))
                emit(UiStateModel.Success(listOf(testCities.first())))
            }

        // When
        viewModel.refreshQuery("Denver")

        // Then
        val cities = viewModel.listState.cityList.value
        assertEquals(1, cities.size)
        assertEquals("Denver", cities.first().name)
    }

    @Test
    fun `toggleFavorite should call use case`() {
        // Given
        coEvery { toggleFavoriteUseCase("1") } returns Unit

        // When
        viewModel.toggleFavorite("1")

        // Then
        coVerify { toggleFavoriteUseCase("1") }
    }

    @Test
    fun `updateConnectionStatus to connected clears error`() {
        // Given
        viewModel.refreshError("No data")

        // When
        viewModel.updateConnectionStatus(true)

        // Then
        assertEquals("", viewModel.listState.errorState.value)
    }

    @Test
    fun `updateConnectionStatus to disconnected sets error`() {
        // When
        viewModel.updateConnectionStatus(false)

        // Then
        assertTrue(
            viewModel.listState.errorState.value
                .isNotBlank(),
        )
    }

    @Test
    fun `refreshError should update error state`() {
        // When
        viewModel.refreshError("Test error")

        // Then
        assertEquals("Test error", viewModel.listState.errorState.value)
    }

    @Test
    fun `refreshLoading should update loading state`() {
        // When
        viewModel.refreshLoading(true)

        // Then
        assertTrue(viewModel.listState.loadingState.value)
    }

    @Test
    fun `refreshFavoriteFilter should update filter and refresh query`() {
        // Given
        coEvery {
            searchCityUseCase(any(), any())
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshFavoriteFilter(true, "Denver")

        // Then
        assertTrue(viewModel.listState.favoriteFilterState.value)
    }

    @Test
    fun `refreshQuery with empty string should return all cities`() {
        // Given
        coEvery {
            searchCityUseCase("", false)
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshQuery("")

        // Then
        val cities = viewModel.listState.cityList.value
        assertEquals(2, cities.size)
    }

    @Test
    fun `refreshQuery with special characters should handle gracefully`() {
        // Given
        coEvery {
            searchCityUseCase("@#\$%", false)
        } returns flow { emit(UiStateModel.Success(emptyList())) }

        // When
        viewModel.refreshQuery("@#\$%")

        // Then
        val cities = viewModel.listState.cityList.value
        assertEquals(0, cities.size)
    }

    @Test
    fun `refreshLoading with false should update loading state`() {
        // When
        viewModel.refreshLoading(false)

        // Then
        assertFalse(viewModel.listState.loadingState.value)
    }

    @Test
    fun `refreshError with empty message should clear error`() {
        // Given
        viewModel.refreshError("Some error")

        // When
        viewModel.refreshError("")

        // Then
        assertEquals("", viewModel.listState.errorState.value)
    }

    @Test
    fun `refreshFavoriteFilter with false and query should update filter`() {
        // Given
        coEvery {
            searchCityUseCase("Denver", false)
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshFavoriteFilter(false, "Denver")

        // Then
        assertFalse(viewModel.listState.favoriteFilterState.value)
    }

    @Test
    fun `refreshFavoriteFilter with true and empty query should update filter`() {
        // Given
        coEvery {
            searchCityUseCase("", true)
        } returns flow { emit(UiStateModel.Success(testCities)) }

        // When
        viewModel.refreshFavoriteFilter(true, "")

        // Then
        assertTrue(viewModel.listState.favoriteFilterState.value)
    }

    @Test
    fun `refresh should load cities with current query`() {
        // Given
        coEvery { searchCityUseCase("", false) } returns
            flow {
                emit(UiStateModel.Loading(true))
                emit(UiStateModel.Success(testCities))
            }

        // When
        viewModel.refresh()

        // Then
        val cities = viewModel.listState.cityList.value
        assertEquals(2, cities.size)
    }

    @Test
    fun `favoriteEvents should emit Added when toggling non-favorite city`() =
        runTest {
            // Given
            coEvery { searchCityUseCase(any(), any()) } returns
                flow { emit(UiStateModel.Success(testCities)) }
            coEvery { toggleFavoriteUseCase("1") } returns Unit
            viewModel.refreshQuery("")

            // When / Then
            viewModel.favoriteEvents.test {
                viewModel.toggleFavorite("1")
                val event = awaitItem()
                assertEquals(FavoriteEvent.Added, event)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `favoriteEvents should emit Removed when toggling favorite city`() =
        runTest {
            // Given
            val favoriteCity = testCities.first().copy(isFavorite = true)
            coEvery { searchCityUseCase(any(), any()) } returns
                flow { emit(UiStateModel.Success(listOf(favoriteCity))) }
            coEvery { toggleFavoriteUseCase("1") } returns Unit
            viewModel.refreshQuery("")

            // When / Then
            viewModel.favoriteEvents.test {
                viewModel.toggleFavorite("1")
                val event = awaitItem()
                assertEquals(FavoriteEvent.Removed, event)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
