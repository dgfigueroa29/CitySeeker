package com.boa.test.city.seeker.presentation.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.data.source.PreferenceDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val preferenceDataSource: PreferenceDataSource,
        private val analyticsService: AnalyticsService,
    ) : ViewModel() {
        private val _showConsent = MutableStateFlow(false)
        val showConsent: StateFlow<Boolean> = _showConsent.asStateFlow()

        init {
            viewModelScope.launch {
                val consented = preferenceDataSource.getAnalyticsConsent()
                analyticsService.setConsentGranted(consented)
                _showConsent.value = !consented
            }
        }

        fun acceptConsent() {
            viewModelScope.launch {
                preferenceDataSource.setAnalyticsConsent(true)
                analyticsService.setConsentGranted(true)
                _showConsent.value = false
            }
        }

        fun declineConsent() {
            viewModelScope.launch {
                preferenceDataSource.setAnalyticsConsent(false)
                analyticsService.setConsentGranted(false)
                _showConsent.value = false
            }
        }
    }
