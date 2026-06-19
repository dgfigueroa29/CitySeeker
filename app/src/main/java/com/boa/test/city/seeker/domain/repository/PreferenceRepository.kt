package com.boa.test.city.seeker.domain.repository

/**
 * Repository interface for managing user preferences related to city IDs.
 */
interface PreferenceRepository {
    suspend fun toggleString(cityId: String)

    suspend fun getSetString(): Set<String>

    suspend fun hasString(cityId: String): Boolean
}
