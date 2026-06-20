package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.common.analytics.AnalyticsEvent
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.PerformanceMonitor
import com.boa.test.city.seeker.domain.usecase.AddJournalEntryUseCase
import com.boa.test.city.seeker.domain.usecase.GetCityByIdUseCase
import com.boa.test.city.seeker.domain.usecase.ToggleFavoriteUseCase
import com.boa.test.city.seeker.presentation.feature.city.list.FavoriteEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val getCityByIdUseCase: GetCityByIdUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
        private val addJournalEntryUseCase: AddJournalEntryUseCase,
        private val analyticsService: AnalyticsService,
        private val performanceMonitor: PerformanceMonitor,
    ) : ViewModel() {
        val detailState = DetailState()

        private val _favoriteEvents = Channel<FavoriteEvent>(Channel.BUFFERED)
        val favoriteEvents: Flow<FavoriteEvent> = _favoriteEvents.receiveAsFlow()

        @OptIn(FlowPreview::class)
        fun getCity(cityId: Long) {
            performanceMonitor.start("get_city_$cityId")
            refreshLoading(true)
            viewModelScope.launch {
                getCityByIdUseCase.invoke(cityId).collectLatest { resource ->
                    if (resource.data != null && resource.message.isBlank()) {
                        detailState.setCity(resource.data)
                        refreshLoading(resource.isLoading)
                        analyticsService.track(AnalyticsEvent.City.View(cityId.toString(), resource.data.name))
                        performanceMonitor.stop("get_city_$cityId")
                    }

                    if (resource.message.isNotBlank() && resource.data == null) {
                        refreshError(resource.message)
                        refreshLoading(resource.isLoading)
                        performanceMonitor.stop("get_city_$cityId")
                    }
                }
            }
        }

        fun toggleFavorite(cityId: String) {
            viewModelScope.launch {
                val wasFavorite = detailState.city.value.isFavorite
                toggleFavoriteUseCase.invoke(cityId)
                detailState.setFavorite()
                analyticsService.track(AnalyticsEvent.City.ToggleFavorite(cityId, !wasFavorite))
                val event = if (wasFavorite) FavoriteEvent.Removed else FavoriteEvent.Added
                _favoriteEvents.send(event)
            }
        }

        fun addJournalEntry(
            title: String,
            notes: String,
            rating: Int,
            photoUri: String? = null,
        ) {
            viewModelScope.launch {
                val cityId = detailState.city.value.id
                if (cityId == 0L) return@launch
                addJournalEntryUseCase(cityId, title, notes, rating, photoUri)
                analyticsService.track(AnalyticsEvent.Journal.EntryCreated(cityId.toString()))
            }
        }

        fun refreshError(message: String) {
            detailState.setError(message)
        }

        fun refreshLoading(flag: Boolean) {
            detailState.setLoading(flag)
        }
    }
