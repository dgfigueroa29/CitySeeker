package com.boa.test.city.seeker.presentation.feature.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.usecase.GetCityByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArCityViewModel
    @Inject
    constructor(
        private val getCityByIdUseCase: GetCityByIdUseCase,
    ) : ViewModel() {
        private val _city = MutableStateFlow<CityModel?>(null)
        val city = _city.asStateFlow()

        fun loadCity(cityId: Long) {
            viewModelScope.launch {
                getCityByIdUseCase(cityId).collect { state ->
                    if (state is UiStateModel.Success) {
                        _city.value = state.data
                    }
                }
            }
        }
    }
