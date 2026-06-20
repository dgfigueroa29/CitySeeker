package com.boa.test.city.seeker.data.repository

import com.boa.test.city.seeker.data.source.PreferenceDataSource
import com.boa.test.city.seeker.domain.repository.PreferenceRepository

class PreferenceRepositoryImpl(
    private val preferenceDataSource: PreferenceDataSource,
) : PreferenceRepository {
    override suspend fun toggleString(cityId: String) {
        preferenceDataSource.toggleString(cityId)
    }

    override suspend fun getSetString(): Set<String> = preferenceDataSource.getSetString()

    override suspend fun hasString(cityId: String): Boolean = preferenceDataSource.hasString(cityId)

    override suspend fun addSearchQuery(query: String) {
        preferenceDataSource.addSearchQuery(query)
    }

    override suspend fun getSearchHistory(): List<String> = preferenceDataSource.getSearchHistory()

    override suspend fun clearSearchHistory() {
        preferenceDataSource.clearSearchHistory()
    }

    override suspend fun setThemeMode(modeName: String) {
        preferenceDataSource.setThemeMode(modeName)
    }

    override suspend fun getThemeMode(): String = preferenceDataSource.getThemeMode()
}
