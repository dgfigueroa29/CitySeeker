package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import javax.inject.Inject

/**
 * Use case for toggling the favorite status of a city.
 *
 * This class encapsulates the business logic for adding or removing a city
 * from the user's list of favorite cities. It interacts with the
 * [PreferenceRepository] to persist these changes.
 *
 * @property preferenceRepository The repository responsible for managing user preferences,
 *                                including favorite cities.
 */
class ToggleFavoriteUseCase @Inject constructor(private val preferenceRepository: PreferenceRepository) {
    /**
     * Toggles the favorite status of a city.
     *
     * This function updates the favorite status of a city in the repository.
     *
     * @param cityId The ID of the city to toggle the favorite status for.
     */
    suspend operator fun invoke(cityId: String) {
        preferenceRepository.toggleString(cityId)
    }
}
