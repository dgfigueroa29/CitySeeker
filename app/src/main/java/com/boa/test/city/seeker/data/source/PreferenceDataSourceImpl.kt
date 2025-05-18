package com.boa.test.city.seeker.data.source

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of [PreferenceDataSource] that uses [DataStore] to persist
 * a set of favorite city IDs.
 *
 * This class provides methods to add, remove, retrieve, and check for the existence
 * of city IDs in the DataStore. It relies on a [Preferences.Key] named `FAVORITE_CITIES`
 * to store the set of strings.
 *
 * @property dataStore The [DataStore] instance used for persisting preferences.
 */
class PreferenceDataSourceImpl @Inject constructor(private val dataStore: DataStore<Preferences>) :
    PreferenceDataSource {
    /**
     * Toggles the presence of a city ID in the set of favorite cities in the DataStore.
     *
     * If the `cityId` is currently in the set, it will be removed.
     * If the `cityId` is not in the set, it will be added.
     * This operation is performed atomically within a DataStore transaction.
     *
     * @param cityId The ID of the city to add or remove from the favorites.
     */
    override suspend fun toggleString(cityId: String) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_CITIES] ?: emptySet()

            val newFavorites = if (currentFavorites.contains(cityId)) {
                currentFavorites - cityId
            } else {
                currentFavorites + cityId
            }

            preferences[FAVORITE_CITIES] = newFavorites
        }
    }

    /**
     * Retrieves the current set of favorite city IDs from the DataStore.
     *
     * This function accesses the DataStore's data Flow, takes the first emitted
     * Preferences object (representing the current state), and then extracts the
     * Set<String> associated with the `FAVORITE_CITIES` key. If the key is not
     * found or the value is null, it defaults to an empty Set<String>.
     *
     * This is a suspending function and should be called from a coroutine or another
     * suspending function.
     *
     * @return The Set<String> of favorite city IDs, or an empty set if none are stored.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getSetString(): Set<String> =
        dataStore.data.first()[FAVORITE_CITIES]?.toSet() ?: emptySet()

    /**
     * Checks if a specific city ID exists within the set of favorite cities in the DataStore.
     *
     * This function accesses the DataStore's data Flow, takes the first emitted Preferences object,
     * and then checks if the Set<String> associated with the `FAVORITE_CITIES` key
     * contains the provided `cityId`.
     *
     * @param cityId The ID of the city to check for existence.
     * @return `true` if the `cityId` is found in the set of favorite cities, `false` otherwise.
     */
    override suspend fun hasString(cityId: String): Boolean {
        val savedData = dataStore.data.first()[FAVORITE_CITIES]?.toSet() ?: emptySet()
        return savedData.contains(cityId)
    }

    companion object {
        /**
         * A [Preferences.Key] for storing a set of favorite city IDs as strings.
         * This key is used to access and modify the set of favorite cities within the DataStore.
         */
        val FAVORITE_CITIES = stringSetPreferencesKey("favorite_cities")
    }
}
