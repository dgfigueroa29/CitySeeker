package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val searchCityUseCase: SearchCityUseCase
) : ViewModel() {
    val listState = ListState()
    private var isConnected = true

    fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (isConnected) {
            refreshError("")
        } else {
            refreshError("No data to display. Please restart your connection or your app to continue.\n")
        }
    }

    fun getCities(textFilter: String) {
        refreshLoading(textFilter.isEmpty())
        //Force loading at the beginning
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

    fun refreshQuery(query: String) {
        listState.setQuery(query)
        getCities(query)
    }

    fun refreshError(message: String) {
        listState.setError(message)
    }

    fun refreshLoading(flag: Boolean) {
        listState.setLoading(flag)
    }

    fun refreshFavoriteFilter(flag: Boolean) {
        listState.setFavoriteFilter(flag)
    }
}
