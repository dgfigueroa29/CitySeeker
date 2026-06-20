package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.common.analytics.AnalyticsEvent
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.PerformanceMonitor
import com.boa.test.city.seeker.domain.usecase.RecordSearchUseCase
import com.boa.test.city.seeker.domain.usecase.SearchCityUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FavoriteEvent {
    data object Added : FavoriteEvent

    data object Removed : FavoriteEvent
}

@HiltViewModel
class ListViewModel
    @Inject
    constructor(
        private val searchCityUseCase: SearchCityUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
        private val recordSearchUseCase: RecordSearchUseCase,
        private val analyticsService: AnalyticsService,
        private val performanceMonitor: PerformanceMonitor,
    ) : ViewModel() {
        val listState = ListState()
        private var isConnected = true

        private val _favoriteEvents = Channel<FavoriteEvent>(Channel.BUFFERED)
        val favoriteEvents: Flow<FavoriteEvent> = _favoriteEvents.receiveAsFlow()

        private var searchJob: Job? = null
        private var initialized = false

        @Suppress("unused")
        fun updateConnectionStatus(isConnected: Boolean) {
            this.isConnected = isConnected

            if (isConnected) {
                refreshError("")
            } else {
                refreshError("No data to display. Please restart your connection or your app to continue.\n")
            }
        }

        @OptIn(FlowPreview::class)
        private fun getCities(
            textFilter: String,
            debounce: Boolean = false,
        ) {
            searchJob?.cancel()
            searchJob =
                viewModelScope.launch {
                    val queryFlow =
                        if (debounce) {
                            kotlinx.coroutines.flow
                                .flowOf(textFilter)
                                .debounce(300L)
                                .distinctUntilChanged()
                        } else {
                            kotlinx.coroutines.flow.flowOf(textFilter)
                        }

                    queryFlow.collect { query ->
                        if (query.isNotEmpty()) {
                            viewModelScope.launch {
                                recordSearchUseCase(query)
                            }
                        }
                        performanceMonitor.start("search_cities")
                        searchCityUseCase
                            .invoke(query, listState.favoriteFilterState.value)
                            .collect { resource ->
                                if (resource.data != null && resource.message.isBlank()) {
                                    listState.setList(resource.data)
                                    refreshLoading(resource.isLoading)
                                    performanceMonitor.stop("search_cities")
                                    return@collect
                                }

                                if (resource.message.isNotBlank() && resource.data == null) {
                                    refreshError(resource.message)
                                    refreshLoading(resource.isLoading)
                                    performanceMonitor.stop("search_cities")
                                    return@collect
                                }
                            }
                    }
                }
        }

        fun toggleFavorite(cityId: String) {
            viewModelScope.launch {
                val wasFavorite =
                    listState.cityList.value
                        .find { it.id.toString() == cityId }
                        ?.isFavorite
                toggleFavoriteUseCase.invoke(cityId)
                listState.setFavorite(cityId)
                analyticsService.track(AnalyticsEvent.City.ToggleFavorite(cityId, wasFavorite != true))
                val event = if (wasFavorite == true) FavoriteEvent.Removed else FavoriteEvent.Added
                _favoriteEvents.send(event)
            }
        }

        fun load() {
            refreshLoading(true)
            refreshQuery("")
        }

        fun refresh() {
            refreshLoading(true)
            getCities(listState.queryState.value)
        }

        fun refreshQuery(
            query: String,
            debounce: Boolean = false,
        ) {
            listState.setQuery(query)
            getCities(query, debounce)
        }

        fun refreshError(message: String) {
            listState.setError(message)
        }

        fun refreshLoading(flag: Boolean) {
            listState.setLoading(flag)
        }

        fun refreshFavoriteFilter(
            withOnlyFavorites: Boolean,
            query: String,
        ) {
            listState.setFavoriteFilter(withOnlyFavorites)
            refreshQuery(query)
        }
    }
