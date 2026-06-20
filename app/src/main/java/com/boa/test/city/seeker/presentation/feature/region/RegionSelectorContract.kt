package com.boa.test.city.seeker.presentation.feature.region

import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegionSelectorState(
    private val _countries: MutableStateFlow<List<String>> = MutableStateFlow(emptyList()),
    val countries: StateFlow<List<String>> = _countries.asStateFlow(),
    private val _selectedCountry: MutableStateFlow<String?> = MutableStateFlow(null),
    val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow(),
    private val _cities: MutableStateFlow<List<CityModel>> = MutableStateFlow(emptyList()),
    val cities: StateFlow<List<CityModel>> = _cities.asStateFlow(),
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow(),
    private val _errorState: MutableStateFlow<String> = MutableStateFlow(""),
    val errorState: StateFlow<String> = _errorState.asStateFlow(),
) {
    fun setCountries(countries: List<String>) {
        _countries.value = countries
    }

    fun setSelectedCountry(country: String?) {
        _selectedCountry.value = country
    }

    fun setCities(cities: List<CityModel>) {
        _cities.value = cities
    }

    fun setLoading(loading: Boolean) {
        _loadingState.value = loading
    }

    fun setError(error: String) {
        _errorState.value = error
    }
}
