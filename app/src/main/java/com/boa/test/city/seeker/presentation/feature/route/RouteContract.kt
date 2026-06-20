package com.boa.test.city.seeker.presentation.feature.route

import com.boa.test.city.seeker.domain.model.CityModel

data class RouteState(
    val cities: List<CityModel> = emptyList(),
    val isLoading: Boolean = false,
)
