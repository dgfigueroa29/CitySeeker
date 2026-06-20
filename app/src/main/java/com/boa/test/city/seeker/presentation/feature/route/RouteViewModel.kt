package com.boa.test.city.seeker.presentation.feature.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.GetRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel
    @Inject
    constructor(
        private val getRecommendationsUseCase: GetRecommendationsUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(RouteState())
        val state = _state.asStateFlow()

        init {
            regenerate()
        }

        fun regenerate() {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)
                val cities = getRecommendationsUseCase()
                _state.value = RouteState(cities = cities, isLoading = false)
            }
        }
    }
