package com.boa.test.city.seeker.domain.model

data class CityModel(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    var isFavorite: Boolean = false
)
