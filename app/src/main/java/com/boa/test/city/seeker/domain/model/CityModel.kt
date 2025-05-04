package com.boa.test.city.seeker.domain.model

data class CityModel(
    val id: Long = 0L,
    val name: String = "",
    val country: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    var isFavorite: Boolean = false
)
