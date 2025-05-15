package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
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
    private val searchCityUseCase: SearchCityUseCase
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
            searchCityUseCase.invoke(textFilter).collect { resource ->
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
     * Toggles the favorite filter state.
     *
     * @param flag A boolean indicating whether the favorite filter should be active (true) or inactive (false).
     */
    fun refreshFavoriteFilter(flag: Boolean) {
        listState.setFavoriteFilter(flag)
    }
}
