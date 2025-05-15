package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.GetCityByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the city detail screen.
 *
 * This ViewModel is responsible for fetching and managing the state of the city detail screen.
 * It uses a [GetCityByIdUseCase] to retrieve city information.
 *
 * @property getCityByIdUseCase The use case for getting a city by its ID.
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getCityByIdUseCase: GetCityByIdUseCase
) : ViewModel() {
    val detailState = DetailState()

    /**
     * Retrieves city details for the given city ID.
     *
     * This function launches a coroutine in the `viewModelScope` to execute the `getCityByIdUseCase`.
     * It updates the `detailState` based on the resource's data and loading/error states.
     *
     * @param cityId The ID of the city to retrieve.
     */
    @OptIn(FlowPreview::class)
    fun getCity(cityId: Long) {
        refreshLoading(true)
        viewModelScope.launch {
            getCityByIdUseCase.invoke(cityId).collectLatest { resource ->
                if (resource.data != null && resource.message.isBlank()) {
                    detailState.setCity(resource.data)
                    refreshLoading(resource.isLoading)
                }

                if (resource.message.isNotBlank() && resource.data == null) {
                    refreshError(resource.message)
                    refreshLoading(resource.isLoading)
                }
            }
        }
    }

    /**
     * Sets the error state in the [detailState] with the given message.
     *
     * @param message The error message to set.
     */
    fun refreshError(message: String) {
        detailState.setError(message)
    }

    /**
     * Updates the loading state of the detail screen.
     *
     * @param flag A boolean indicating whether the screen is currently loading (true) or not (false).
     */
    fun refreshLoading(flag: Boolean) {
        detailState.setLoading(flag)
    }
}
