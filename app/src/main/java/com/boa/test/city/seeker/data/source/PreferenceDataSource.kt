package com.boa.test.city.seeker.data.source

/**
 * Represents a data source for managing app preferences via DataStore.
 * Supports favorite city IDs (string set), onboarding state, analytics consent,
 * and search history.
 */
interface PreferenceDataSource {
    suspend fun toggleString(cityId: String)

    suspend fun getSetString(): Set<String>

    suspend fun hasString(cityId: String): Boolean

    suspend fun markOnboardingCompleted()

    suspend fun isOnboardingCompleted(): Boolean

    suspend fun setAnalyticsConsent(granted: Boolean)

    suspend fun getAnalyticsConsent(): Boolean

    suspend fun addSearchQuery(query: String)

    suspend fun getSearchHistory(): List<String>

    suspend fun clearSearchHistory()

    suspend fun setThemeMode(modeName: String)

    suspend fun getThemeMode(): String
}
