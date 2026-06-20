package com.boa.test.city.seeker.domain.repository

/**
 * Repository interface for managing user preferences related to city IDs,
 * onboarding state, analytics consent, and search history.
 */
interface PreferenceRepository {
    suspend fun toggleString(cityId: String)

    suspend fun getSetString(): Set<String>

    suspend fun hasString(cityId: String): Boolean

    suspend fun addSearchQuery(query: String)

    suspend fun getSearchHistory(): List<String>

    suspend fun clearSearchHistory()

    suspend fun setThemeMode(modeName: String)

    suspend fun getThemeMode(): String
}
