@file:Suppress("unused")

package com.boa.test.city.seeker.presentation.feature.city.detail

import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Represents the UI state of the Detail screen, holding information about loading status, errors,
 * and the details of a specific city.
 *
 * @property _loadingState The internal mutable state flow for the loading status.
 * @property loadingState The publicly exposed state flow for the loading status.
 * @property _errorState The internal mutable state flow for the error message.
 * @property errorState The publicly exposed state flow for the error message.
 * @property _city The internal mutable state flow for the CityModel.
 * @property city The publicly exposed state flow for the CityModel.
 */
@Suppress("ConstructorParameterNaming")
class DetailState(
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow(),
    private val _errorState: MutableStateFlow<String> = MutableStateFlow(""),
    val errorState: StateFlow<String> = _errorState.asStateFlow(),
    private val _city: MutableStateFlow<CityModel> = MutableStateFlow(
        CityModel()
    ),
    val city: StateFlow<CityModel> = _city.asStateFlow(),
) {
    fun setCity(city: CityModel) {
        this._city.value = city
    }

    fun setLoading(loading: Boolean) {
        this._loadingState.value = loading
    }

    fun setError(error: String) {
        this._errorState.value = error
    }

    fun setFavorite() {
        val currentCity = this._city.value
        this._city.value = currentCity.copy(isFavorite = !currentCity.isFavorite)
    }
}

/**
 * List Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
sealed interface ListAction {
    data object OnClick : ListAction
}
