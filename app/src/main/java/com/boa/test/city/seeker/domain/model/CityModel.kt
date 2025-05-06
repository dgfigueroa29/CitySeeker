package com.boa.test.city.seeker.domain.model

/**
 * Represents a city with its details, including name, country, geographical coordinates, and favorite status.
 *
 * @property id The unique identifier of the city. Defaults to 0L.
 * @property name The name of the city. Defaults to an empty string.
 * @property country The country where the city is located. Defaults to an empty string.
 * @property latitude The latitude coordinate of the city. Defaults to 0.0.
 * @property longitude The longitude coordinate of the city. Defaults to 0.0.
 * @property isFavorite Indicates whether the city is marked as a favorite. Defaults to false.
 */
data class CityModel(
    val id: Long = 0L,
    val name: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var isFavorite: Boolean = false
) {
    fun getTitle(): String = "$name, $country"

    fun getSubtitle(): String = "Lat: $latitude, Long: $longitude"
}
