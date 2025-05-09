package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.paging.PagingData
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.ui.previewCities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State that represents ListScreen
 **/
@Suppress("ConstructorParameterNaming")
data class ListState(
    private val _loadingState: MutableStateFlow<Boolean> = MutableStateFlow(true),
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow(),
    private val _favoriteFilterState: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val favoriteFilterState: StateFlow<Boolean> = _favoriteFilterState.asStateFlow(),
    private val _textFilterState: MutableStateFlow<String> = MutableStateFlow(""),
    val textFilterState: StateFlow<String> = _textFilterState.asStateFlow(),
    private val _errorState: MutableStateFlow<String> = MutableStateFlow(""),
    val errorState: StateFlow<String> = _errorState.asStateFlow(),
    private val _cityList: MutableStateFlow<PagingData<CityModel>> = MutableStateFlow(
        PagingData.empty()
    ),
    val cityList: StateFlow<PagingData<CityModel>> = _cityList.asStateFlow(),
    private val _listAction: MutableStateFlow<ListAction> = MutableStateFlow(ListAction.OnClick),
    private val _queryState: MutableStateFlow<String> = MutableStateFlow(""),
    val queryState: StateFlow<String> = _errorState.asStateFlow()
) {
    fun setList(data: PagingData<CityModel>) {
        this._cityList.value = data
    }

    fun setLoading(loading: Boolean) {
        this._loadingState.value = loading
    }

    fun setFavoriteFilter(favoriteFilter: Boolean) {
        this._favoriteFilterState.value = favoriteFilter
    }

    fun setError(error: String) {
        this._errorState.value = error
    }

    fun setQuery(query: String) {
        this._queryState.value = query
    }

    fun previewList() {
        this._cityList.value = PagingData.from(previewCities())
    }
}

/**
 * List Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/

sealed interface ListAction {
    data object OnClick : ListAction
}
