package com.boa.test.city.seeker.presentation.feature.city.detail

import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.PerformanceMonitor
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.usecase.GetCityByIdUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private lateinit var viewModel: DetailViewModel
    private lateinit var getCityByIdUseCase: GetCityByIdUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private val analyticsService: AnalyticsService = mockk(relaxed = true)
    private val performanceMonitor: PerformanceMonitor = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val denver = CityModel(1, "Denver", "US", 39.7392, -104.9903)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCityByIdUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        viewModel = DetailViewModel(getCityByIdUseCase, toggleFavoriteUseCase, analyticsService, performanceMonitor)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getDetailState initial state should have loading true`() {
        assertEquals(true, viewModel.detailState.loadingState.value)
        assertEquals("", viewModel.detailState.errorState.value)
        assertEquals(CityModel(), viewModel.detailState.city.value)
    }

    @Test
    fun `getDetailState after successful getCity should update city data`() {
        coEvery { getCityByIdUseCase(1L) } returns
            flow {
                emit(UiStateModel.Loading(true))
                emit(UiStateModel.Success(denver))
                emit(UiStateModel.Loading(false))
            }

        viewModel.getCity(1L)

        assertEquals(denver.name, viewModel.detailState.city.value.name)
        assertEquals(denver.id, viewModel.detailState.city.value.id)
        assertFalse(viewModel.detailState.loadingState.value)
        assertEquals("", viewModel.detailState.errorState.value)
    }

    @Test
    fun `getDetailState after getCity with error should set error message`() {
        coEvery { getCityByIdUseCase(1L) } returns
            flow {
                emit(UiStateModel.Loading(true))
                emit(UiStateModel.Error("City not found"))
                emit(UiStateModel.Loading(false))
            }

        viewModel.getCity(1L)

        assertEquals("City not found", viewModel.detailState.errorState.value)
        assertFalse(viewModel.detailState.loadingState.value)
        assertEquals(CityModel(), viewModel.detailState.city.value)
    }

    @Test
    fun `getDetailState during getCity loading should reflect loading state`() {
        coEvery { getCityByIdUseCase(1L) } returns
            flow {
                emit(UiStateModel.Loading(true))
                emit(UiStateModel.Success(denver))
                emit(UiStateModel.Loading(false))
            }

        viewModel.getCity(1L)

        val wasLoading = viewModel.detailState.loadingState.value
        assertFalse(wasLoading)
    }

    @Test
    fun `getDetailState after toggleFavorite should toggle favorite status`() {
        coEvery { toggleFavoriteUseCase("1") } returns Unit
        coEvery { getCityByIdUseCase(1L) } returns
            flow { emit(UiStateModel.Success(denver)) }

        viewModel.getCity(1L)
        viewModel.toggleFavorite("1")

        assertTrue(viewModel.detailState.city.value.isFavorite)

        viewModel.toggleFavorite("1")
        assertFalse(viewModel.detailState.city.value.isFavorite)
    }

    @Test
    fun `getCity with invalid cityId should handle gracefully`() {
        coEvery { getCityByIdUseCase(any<Long>()) } returns
            flow { emit(UiStateModel.Error("Invalid city ID")) }

        viewModel.getCity(-1)

        assertNotNull(viewModel.detailState.errorState.value)
        assertFalse(viewModel.detailState.loadingState.value)
    }

    @Test
    fun `getCity when use case returns data and message should set data`() {
        coEvery { getCityByIdUseCase(1L) } returns
            flow {
                emit(UiStateModel.Success(denver))
            }

        viewModel.getCity(1L)

        assertEquals(denver.name, viewModel.detailState.city.value.name)
    }

    @Test
    fun `getCity when use case returns no data and no message should not crash`() {
        coEvery { getCityByIdUseCase(1L) } returns
            flow {
                emit(UiStateModel.Loading(false))
            }

        viewModel.getCity(1L)

        assertEquals(CityModel(), viewModel.detailState.city.value)
    }

    @Test
    fun `toggleFavorite should call use case with cityId`() {
        coEvery { toggleFavoriteUseCase("1") } returns Unit

        viewModel.toggleFavorite("1")

        coVerify { toggleFavoriteUseCase("1") }
    }

    @Test
    fun `refreshError with non empty message should update error state`() {
        viewModel.refreshError("Something went wrong")

        assertEquals("Something went wrong", viewModel.detailState.errorState.value)
    }

    @Test
    fun `refreshError with empty message should clear error`() {
        viewModel.refreshError("Previous error")
        viewModel.refreshError("")

        assertEquals("", viewModel.detailState.errorState.value)
    }

    @Test
    fun `refreshLoading with true should update loading state`() {
        viewModel.refreshLoading(true)

        assertTrue(viewModel.detailState.loadingState.value)
    }

    @Test
    fun `refreshLoading with false should update loading state`() {
        viewModel.refreshLoading(false)

        assertFalse(viewModel.detailState.loadingState.value)
    }

    @Test
    fun `toggleFavorite multiple calls should alternate favorite state`() {
        coEvery { toggleFavoriteUseCase("1") } returns Unit

        viewModel.toggleFavorite("1")
        assertTrue(viewModel.detailState.city.value.isFavorite)

        viewModel.toggleFavorite("1")
        assertFalse(viewModel.detailState.city.value.isFavorite)

        viewModel.toggleFavorite("1")
        assertTrue(viewModel.detailState.city.value.isFavorite)
    }
}
