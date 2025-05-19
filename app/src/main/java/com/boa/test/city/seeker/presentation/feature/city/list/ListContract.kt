package com.boa.test.city.seeker.presentation.feature.city.list

import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.presentation.ui.previewCities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Represents the mutable state of the ListScreen UI.
 *
 * This data class holds various pieces of state information used by the ListScreen,
 * such as loading status, filter states, error messages, and the list of cities.
 *
 * @property _loadingState The internal mutable state flow for tracking loading status.
 * @property loadingState The publicly exposed state flow for observing loading status.
 * @property _favoriteFilterState The internal mutable state flow for tracking the favorite filter status.
 * @property favoriteFilterState The publicly exposed state flow for observing the favorite filter status.
 * @property _textFilterState The internal mutable state flow for tracking the text filter status.
 * @property textFilterState The publicly exposed state flow for observing the text filter status.
 * @property _errorState The internal mutable state flow for tracking error messages.
 * @property errorState The publicly exposed state flow for observing error messages.
 * @property _cityList The internal mutable state flow holding the list of cities.
 * @property cityList The publicly exposed state flow for observing the list of cities.
 * @property _listAction The internal mutable state flow for list actions (currently unused in this public interface).
 * @property _queryState The internal mutable state flow for tracking the search query.
 * @property queryState The publicly exposed state flow for observing the search query.
 */
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
    private val _cityList: MutableStateFlow<List<CityModel>> = MutableStateFlow(
        emptyList()
    ),
    val cityList: StateFlow<List<CityModel>> = _cityList.asStateFlow(),
    private val _listAction: MutableStateFlow<ListAction> = MutableStateFlow(ListAction.OnClick),
    private val _queryState: MutableStateFlow<String> = MutableStateFlow(""),
    val queryState: StateFlow<String> = _errorState.asStateFlow()
) {
    fun setList(data: List<CityModel>) {
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
        this._cityList.value = previewCities()
    }

    fun setFavorite(cityId: String) {
        this._cityList.value = this._cityList.value.map {
            if (it.id.toString() == cityId) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                it
            }
        }
    }
}

/**
 * List Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/
sealed interface ListAction {
    data object OnClick : ListAction
}
