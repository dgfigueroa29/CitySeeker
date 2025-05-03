package com.boa.test.city.seeker.data.network

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing a city.
 *
 * This class is used to transfer city information between different parts of the application,
 * or between the application and external systems. It holds the essential details of a city,
 * including its country, name, unique identifier, and geographical coordinates.
 *
 * @property country The country where the city is located (e.g., "USA", "Canada").
 * @property name The name of the city (e.g., "New York", "Toronto").
 * @property id The unique identifier for the city. This is mapped to the "_id" field in the serialized data.
 * @property coord The geographical coordinates (latitude and longitude) of the city.
 */
@Serializable
data class CityDto(
    val country: String,
    val name: String,
    val _id: Int,
    val coord: CoordinatesDto
)
