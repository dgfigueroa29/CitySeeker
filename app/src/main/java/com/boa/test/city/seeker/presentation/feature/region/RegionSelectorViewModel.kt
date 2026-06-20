package com.boa.test.city.seeker.presentation.feature.region

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.GetCitiesByCountryUseCase
import com.boa.test.city.seeker.domain.usecase.GetCountriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionSelectorViewModel
    @Inject
    constructor(
        private val getCountriesUseCase: GetCountriesUseCase,
        private val getCitiesByCountryUseCase: GetCitiesByCountryUseCase,
    ) : ViewModel() {
        val regionState = RegionSelectorState()

        private val _selectedCountry = MutableStateFlow<String?>(null)
        val selectedCountry: StateFlow<String?> = _selectedCountry.asStateFlow()

        init {
            loadCountries()
        }

        private fun loadCountries() {
            regionState.setLoading(true)
            viewModelScope.launch {
                getCountriesUseCase()
                    .catch { e ->
                        regionState.setError(e.message ?: "Failed to load countries")
                        regionState.setLoading(false)
                    }.collectLatest { countries ->
                        regionState.setCountries(countries)
                        regionState.setLoading(false)
                    }
            }
        }

        fun selectCountry(country: String?) {
            _selectedCountry.value = country
            regionState.setSelectedCountry(country)
            if (country != null) {
                loadCitiesByCountry(country)
            } else {
                regionState.setCities(emptyList())
            }
        }

        private fun loadCitiesByCountry(country: String) {
            regionState.setLoading(true)
            viewModelScope.launch {
                getCitiesByCountryUseCase(country)
                    .catch { e ->
                        regionState.setError(e.message ?: "Failed to load cities")
                        regionState.setLoading(false)
                    }.collectLatest { cities ->
                        regionState.setCities(cities)
                        regionState.setLoading(false)
                    }
            }
        }
    }
