package com.boa.test.city.seeker.presentation.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.common.analytics.AnalyticsEvent
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.data.source.PreferenceDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val preferenceDataSource: PreferenceDataSource,
        private val analyticsService: AnalyticsService,
    ) : ViewModel() {
        private val _isLoading = MutableStateFlow(true)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _isCompleted = MutableStateFlow(true)
        val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

        private val _currentPage = MutableStateFlow(0)
        val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

        init {
            analyticsService.track(AnalyticsEvent.Onboarding.Started)
            viewModelScope.launch {
                _isCompleted.value =
                    try {
                        preferenceDataSource.isOnboardingCompleted()
                    } catch (e: Exception) {
                        Timber.e("Failed to read onboarding state: ${e.stackTraceToString()}")
                        true
                    }
                _isLoading.value = false
            }
        }

        fun onPageChanged(page: Int) {
            _currentPage.value = page
        }

        fun completeOnboarding() {
            analyticsService.track(AnalyticsEvent.Onboarding.Completed(slideCount = _currentPage.value + 1))
            viewModelScope.launch {
                preferenceDataSource.markOnboardingCompleted()
                _isCompleted.value = true
            }
        }

        fun skipOnboarding() {
            analyticsService.track(AnalyticsEvent.Onboarding.Skipped(currentSlide = _currentPage.value))
            viewModelScope.launch {
                preferenceDataSource.markOnboardingCompleted()
                _isCompleted.value = true
            }
        }

        fun shouldSkipAfterTwoSlides(): Boolean = _currentPage.value >= 1
    }
