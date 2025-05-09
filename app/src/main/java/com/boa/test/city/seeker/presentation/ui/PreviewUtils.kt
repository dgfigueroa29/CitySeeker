package com.boa.test.city.seeker.presentation.ui

import com.boa.test.city.seeker.domain.model.CityModel

/**
 * Provides a list of sample `CityModel` objects for previewing or testing purposes.
 *
 * This function creates and returns a hardcoded list of `CityModel` instances,
 * each representing a city with predefined attributes like ID, name, country,
 * favorite status, latitude, and longitude.  The data is primarily intended for
 * demonstrating UI components or for testing data flows before integrating with
 * a live data source.
 *
 * The cities included in the list are:
 * - Mendoza (AR) - Marked as favorite.
 * - San Juan (AR) - Not a favorite.
 * - New York (US) - Not a favorite.
 * - Los Angeles (US) - Marked as favorite.
 * - Chicago (US) - Not a favorite.
 *
 * Note: The latitude and longitude values are currently set to 0.0 for all
 * cities in this preview data. In a real-world application, these would
 * represent the actual geographical coordinates of the cities.
 *
 * @return A `List<CityModel>` containing the predefined city data.
 */
fun previewCities(): List<CityModel> {
    return listOf(
        CityModel(
            id = 1,
            name = "Mendoza",
            country = "AR",
            isFavorite = true,
            latitude = -32.8832389,
            longitude = -68.9364586
        ), CityModel(
            id = 2,
            name = "San Juan",
            country = "AR",
            isFavorite = false,
            latitude = 0.0,
            longitude = 0.0
        ),
        CityModel(
            id = 3,
            name = "New York",
            country = "US",
            isFavorite = false,
            latitude = 0.0,
            longitude = 0.0
        ),
        CityModel(
            id = 4,
            name = "Los Angeles",
            country = "US",
            isFavorite = true,
            latitude = 0.0,
            longitude = 0.0
        ),
        CityModel(
            id = 5,
            name = "Chicago",
            country = "US",
            isFavorite = false,
            latitude = 0.0,
            longitude = 0.0
        )
    )
}
