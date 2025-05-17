package com.boa.test.city.seeker.data.source

/**
 * Represents a repository for managing preferences.
 * This interface defines methods for adding, removing, retrieving,
 * and checking the existence of string values in preferences.
 */
interface PreferenceDataSource {
    /**
     * Toggles the presence of a string value in the preferences.
     * If the string value exists, it is removed. If it doesn't exist, it is added.
     *
     * @param cityId The ID of the city to toggle.
     */
    suspend fun toggleString(cityId: String)

    /**
     * Retrieves a flow of sets of strings from the preferences.
     *
     * @return A Flow emitting sets of strings stored in preferences.
     */
    suspend fun getSetString(): Set<String>

    /**
     * Checks if a specific string value exists in the preferences.
     *
     * @param cityId The string value to check for.
     * @return `true` if the string value exists, `false` otherwise.
     */
    suspend fun hasString(cityId: String): Boolean
}
