package com.boa.test.city.seeker.data.repository

import com.boa.test.city.seeker.data.source.PreferenceDataSource
import com.boa.test.city.seeker.domain.repository.PreferenceRepository

/**
 * Implementation of [PreferenceRepository] that uses a [PreferenceDataSource] to manage preferences.
 *
 * @property preferenceDataSource The data source for preferences.
 */
class PreferenceRepositoryImpl(private val preferenceDataSource: PreferenceDataSource) :
    PreferenceRepository {
    /**
     * Toggles the presence of a city ID in the preferences.
     *
     * If the city ID is already present, it will be removed.
     * If the city ID is not present, it will be added.
     *
     * @param cityId The ID of the city to toggle.
     */
    override suspend fun toggleString(cityId: String) {
        preferenceDataSource.toggleString(cityId)
    }
}
