package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetRecommendationsUseCase
@Inject
constructor(
    private val cityRepository: CityRepository,
) {
    suspend operator fun invoke(limit: Int = 10): List<CityModel> =
        withContext(Dispatchers.IO) {
            try {
                cityRepository.getRecommendations(limit)
            } catch (e: Exception) {
                Timber.e("Error getting recommendations: ${e.stackTraceToString()}")
                emptyList()
            }
        }
}
