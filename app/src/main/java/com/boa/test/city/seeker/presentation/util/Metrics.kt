package com.boa.test.city.seeker.presentation.util

import android.annotation.SuppressLint
import com.boa.test.city.seeker.common.analytics.AnalyticsEvent
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.CompositeAnalyticsService
import com.boa.test.city.seeker.common.analytics.SentryAnalyticsProvider
import com.boa.test.city.seeker.common.analytics.TimberAnalyticsProvider

@SuppressLint("StaticFieldLeak")
object Metrics {
    @Volatile
    private var _analyticsService: AnalyticsService? = null

    private val analyticsService: AnalyticsService
        get() {
            var service = _analyticsService
            if (service == null) {
                synchronized(this) {
                    service = _analyticsService
                    if (service == null) {
                        service =
                            CompositeAnalyticsService(
                                setOf(TimberAnalyticsProvider(), SentryAnalyticsProvider()),
                            )
                        _analyticsService = service
                    }
                }
            }
            return service!!
        }

    fun trackScrollDepth(index: Int) {
        analyticsService.track(AnalyticsEvent.Scroll.Depth(index))
    }

    fun trackSearch(query: String) {
        analyticsService.track(AnalyticsEvent.Search.Query(query, resultCount = 0, durationMs = 0))
    }

    fun trackViewItem(cityId: String) {
        analyticsService.track(AnalyticsEvent.City.View(cityId, ""))
    }

    fun trackToggleFavorite(
        cityId: String,
        isFavorite: Boolean,
    ) {
        analyticsService.track(AnalyticsEvent.City.ToggleFavorite(cityId, isFavorite))
    }
}
