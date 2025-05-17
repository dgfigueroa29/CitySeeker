package com.boa.test.city.seeker.domain.repository

/**
 * Repository interface for managing user preferences related to city IDs.
 *
 * This interface provides a method to toggle the presence of a city ID in the user's preferences.
 */
fun interface PreferenceRepository {
    /**
     * Toggles the presence of a city ID in the user's preferences.
     *
     * If the city ID is already present, it will be removed.
     * If the city ID is not present, it will be added.
     *
     * @param cityId The ID of the city to toggle.
     */
    suspend fun toggleString(cityId: String)
}
