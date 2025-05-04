package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.paging.PagingData
import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Object used for a type safe destination to a List route
 */
object ListDestination

/**
 * UI State that represents ListScreen
 **/
data class ListState(
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow(),
    private val _favoriteFilterState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val favoriteFilterState: StateFlow<Boolean> = _favoriteFilterState.asStateFlow(),
    private val _textFilterState: MutableStateFlow<String> = MutableStateFlow(""),
    val textFilterState: StateFlow<String> = _textFilterState.asStateFlow(),
    private val _errorState: MutableStateFlow<String> = MutableStateFlow(""),
    val errorState: StateFlow<String> = _errorState.asStateFlow(),
    private val _locationList: MutableStateFlow<PagingData<CityModel>> = MutableStateFlow(
        PagingData.empty()
    ),
    val locationList: StateFlow<PagingData<CityModel>> = _locationList.asStateFlow()
) {
    fun setList(data: PagingData<CityModel>) {
        this._locationList.value = data
    }

    fun setLoading(loading: Boolean) {
        this._loadingState.value = loading
    }

    fun setFavoriteFilter(favoriteFilter: Boolean) {
        this._favoriteFilterState.value = favoriteFilter
    }

    fun setFavoriteFilter(textFilter: String) {
        this._textFilterState.value = textFilter
    }

    fun setError(error: String) {
        this._errorState.value = error
    }
}

/**
 * List Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/

sealed interface ListAction {
    data object OnClick : ListAction
}
