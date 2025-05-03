package com.boa.test.city.seeker.data.network

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing geographical coordinates.
 *
 * This class encapsulates longitude and latitude values, typically used for
 * representing a location on the Earth's surface.
 *
 * @property lon The longitude of the location, in degrees.
 *   Longitude values range from -180 to +180, where:
 *   - Negative values represent west of the Prime Meridian.
 *   - Positive values represent east of the Prime Meridian.
 *   - 0 represents the Prime Meridian.
 *
 * @property lat The latitude of the location, in degrees.
 *   Latitude values range from -90 to +90, where:
 *   - Negative values represent south of the Equator.
 *   - Positive values represent north of the Equator.
 *   - 0 represents the Equator.
 */
@Serializable
data class CoordinatesDto(
    val lon: Double,
    val lat: Double
)
