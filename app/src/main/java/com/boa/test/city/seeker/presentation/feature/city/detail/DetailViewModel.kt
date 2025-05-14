package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.lifecycle.ViewModel
import com.boa.test.city.seeker.domain.usecase.GetCityByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCityByIdUseCase: GetCityByIdUseCase
) : ViewModel() {

    private val _stateFlow: MutableStateFlow<DetailState> = MutableStateFlow(DetailState())

    val stateFlow: StateFlow<DetailState> = _stateFlow.asStateFlow()
}
