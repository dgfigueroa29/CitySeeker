package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private lateinit var useCase: ToggleFavoriteUseCase
    private lateinit var preferenceRepository: PreferenceRepository

    @Before
    fun setup() {
        preferenceRepository = mockk()
        useCase = ToggleFavoriteUseCase(preferenceRepository)
    }

    @Test
    fun `should call repository toggleString with cityId`() =
        runTest {
            // Given
            coEvery { preferenceRepository.toggleString("123") } returns Unit

            // When
            useCase("123")

            // Then
            coVerify { preferenceRepository.toggleString("123") }
        }

    @Test
    fun `should handle empty cityId`() =
        runTest {
            // Given
            coEvery { preferenceRepository.toggleString("") } returns Unit

            // When
            useCase("")

            // Then
            coVerify { preferenceRepository.toggleString("") }
        }

    @Test
    fun `should handle large cityId`() =
        runTest {
            // Given
            val largeId = "999999999"
            coEvery { preferenceRepository.toggleString(largeId) } returns Unit

            // When
            useCase(largeId)

            // Then
            coVerify { preferenceRepository.toggleString(largeId) }
        }

    @Test
    fun `should handle special characters in cityId`() =
        runTest {
            // Given
            val specialId = "123-456"
            coEvery { preferenceRepository.toggleString(specialId) } returns Unit

            // When
            useCase(specialId)

            // Then
            coVerify { preferenceRepository.toggleString(specialId) }
        }
}
