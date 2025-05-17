package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the city list screen.
 *
 * This ViewModel manages the state of the city list, including the displayed cities,
 * search query, loading state, error messages, and favorite filtering. It interacts
 * with the [SearchCityUseCase] to retrieve city data based on user input and
 * updates the [listState] accordingly.
 *
 * @property searchCityUseCase The use case responsible for searching cities.
 */
@HiltViewModel
class ListViewModel @Inject constructor(
    private val searchCityUseCase: SearchCityUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {
    val listState = ListState()
    private var isConnected = true

    /**
     * Updates the connection status of the application and updates the error message accordingly.
     *
     * @param isConnected A boolean indicating whether the application is connected to the internet.
     */
    fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (isConnected) {
            refreshError("")
        } else {
            refreshError("No data to display. Please restart your connection or your app to continue.\n")
        }
    }

    /**
     * Initiates the search for cities based on the provided text filter.
     *
     * This function launches a coroutine within the ViewModel's scope to execute the
     * [searchCityUseCase]. It collects the latest resource from the use case and
     * updates the [listState] based on the resource's data, error message, and loading state.
     *
     * @param textFilter The text to use for filtering the city list.
     */
    @OptIn(FlowPreview::class)
    private fun getCities(textFilter: String) {
        viewModelScope.launch {
            searchCityUseCase.invoke(textFilter, listState.favoriteFilterState.value)
                .collect { resource ->
                    if (resource.data != null && resource.message.isBlank()) {
                        listState.setList(resource.data)
                        refreshLoading(resource.isLoading)
                        return@collect
                    }

                    if (resource.message.isNotBlank() && resource.data == null) {
                        refreshError(resource.message)
                        refreshLoading(resource.isLoading)
                        return@collect
                    }
                }
        }
    }

    /**
     * Toggles the favorite status of a city.
     *
     * This function launches a coroutine to call the [ToggleFavoriteUseCase] with the given city ID.
     * The use case will handle the logic to either add or remove the city from the favorites.
     *
     * @param cityId The unique identifier of the city whose favorite status needs to be toggled.
     */
    fun toggleFavorite(cityId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase.invoke(cityId.toString())
        }
    }

    /**
     * Initiates the loading process for the city list.
     * Resets the loading state to true and clears any existing search query.
     */
    fun load() {
        refreshLoading(true)
        refreshQuery("")
    }

    /**
     * Refreshes the query and triggers a new search for cities.
     *
     * @param query The new query string to search for.
     */
    fun refreshQuery(query: String) {
        listState.setQuery(query)
        getCities(query)
    }

    /**
     * Updates the error message in the list state.
     *
     * @param message The error message to display.
     */
    fun refreshError(message: String) {
        listState.setError(message)
    }

    /**
     * Updates the loading state of the list.
     *
     * @param flag `true` if the list is currently loading, `false` otherwise.
     */
    fun refreshLoading(flag: Boolean) {
        listState.setLoading(flag)
    }

    /**
     * Refreshes the favorite filter and updates the city list accordingly.
     *
     * This function updates the favorite filter setting in the [listState] and then
     * triggers a refresh of the city list by calling [refreshQuery] with the provided
     * query. This ensures that the displayed list of cities reflects the new
     * favorite filter state.
     *
     * @param withOnlyFavorites `true` to filter the list to show only favorite cities,
     *                          `false` to show all cities.
     * @param query The current search query string. This is used to re-filter the list
     *              after the favorite filter has been applied.
     */
    fun refreshFavoriteFilter(withOnlyFavorites: Boolean, query: String) {
        listState.setFavoriteFilter(withOnlyFavorites)
        refreshQuery(query)
    }
}
