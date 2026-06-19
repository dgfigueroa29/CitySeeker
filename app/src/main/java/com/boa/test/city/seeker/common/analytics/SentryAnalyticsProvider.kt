package com.boa.test.city.seeker.common.analytics

import io.sentry.Breadcrumb
import io.sentry.Sentry
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentryAnalyticsProvider
@Inject
constructor() : AnalyticsService {
    override fun track(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.Error.Occurred -> Sentry.captureException(event.throwable)
            is AnalyticsEvent.City.ToggleFavorite ->
                Sentry.addBreadcrumb(
                    Breadcrumb().apply {
                        message = "Favorite toggled"
                        type = "user"
                        event.properties.forEach { (k, v) -> setData(k, v) }
                    },
                )

            else ->
                Sentry.addBreadcrumb(
                    Breadcrumb().apply {
                        message = event.name
                        type = "analytics"
                        event.properties.forEach { (k, v) -> setData(k, v.toString()) }
                    },
                )
        }
        Timber.tag(TAG).d("[SENTRY] %s: %s", event.name, event.properties)
    }

    override fun setUserId(userId: String) {
        Sentry.setUser(
            io.sentry.protocol
                .User()
                .apply { id = userId },
        )
    }

    override fun setUserProperty(
        key: String,
        value: String,
    ) {
        Sentry.setTag(key, value)
    }

    companion object {
        private const val TAG = "SentryAnalytics"
    }
}
