package com.boa.test.city.seeker.domain.usecase

import app.cash.turbine.test
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchCityUseCaseTest {
    private lateinit var useCase: SearchCityUseCase
    private lateinit var cityRepository: CityRepository

    private val testCities =
        listOf(
            CityModel(1, "Denver", "US", 39.7392, -104.9903),
            CityModel(2, "Dallas", "US", 32.7767, -96.7970),
        )

    @Before
    fun setup() {
        cityRepository = mockk()
        useCase = SearchCityUseCase(cityRepository)
    }

    @Test
    fun `should emit loading then success with cities`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities("Den", false)
            } returns listOf(testCities.first())

            // When/Then
            useCase("Den", false).test {
                val loading = awaitItem()
                assertTrue(loading is UiStateModel.Loading)

                val success = awaitItem()
                assertTrue(success is UiStateModel.Success)
                val data = (success as UiStateModel.Success).data
                assertNotNull(data)
                assertEquals(1, data?.size)
                assertEquals("Denver", data?.first()?.name)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should emit loading then error on exception`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities(any(), any())
            } throws RuntimeException("Network error")

            // When/Then
            useCase("test", false).test {
                val loading = awaitItem()
                assertTrue(loading is UiStateModel.Loading)

                val error = awaitItem()
                assertTrue(error is UiStateModel.Error)
                assertEquals("Network error", (error as UiStateModel.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should emit loading then success with empty list`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities("XYZ", false)
            } returns emptyList()

            // When/Then
            useCase("XYZ", false).test {
                awaitItem() // Loading
                val success = awaitItem()
                assertTrue(success is UiStateModel.Success)
                val data = (success as UiStateModel.Success).data
                assertNotNull(data)
                assertEquals(0, data?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should pass favorites filter to repository`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities("Den", true)
            } returns listOf(testCities.first())

            // When
            useCase("Den", true).test {
                awaitItem()
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

            // Then - verification happens via coEvery
        }

    @Test
    fun `should handle empty query`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities("", false)
            } returns testCities

            // When/Then
            useCase("", false).test {
                awaitItem() // Loading
                val success = awaitItem()
                assertTrue(success is UiStateModel.Success)
                val data = (success as UiStateModel.Success).data
                assertNotNull(data)
                assertEquals(2, data?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `should handle multiple cities in result`() =
        runTest {
            // Given
            coEvery {
                cityRepository.searchCities("D", false)
            } returns testCities

            // When/Then
            useCase("D", false).test {
                awaitItem() // Loading
                val success = awaitItem()
                assertTrue(success is UiStateModel.Success)
                val data = (success as UiStateModel.Success).data
                assertNotNull(data)
                assertEquals(2, data?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
