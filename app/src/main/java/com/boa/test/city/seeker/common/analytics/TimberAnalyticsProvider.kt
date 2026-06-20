package com.boa.test.city.seeker.common.analytics

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimberAnalyticsProvider
    @Inject
    constructor() : AnalyticsService {
        override fun track(event: AnalyticsEvent) {
            val props = event.properties.entries.joinToString(", ") { "${it.key}=${it.value}" }
            Timber.tag(TAG).d("[%s] %s", event.name, props)
        }

        override fun setUserId(userId: String) {
            Timber.tag(TAG).d("User ID: %s", userId)
        }

        override fun setUserProperty(
            key: String,
            value: String,
        ) {
            Timber.tag(TAG).d("Property: %s=%s", key, value)
        }

        companion object {
            private const val TAG = "Analytics"
        }
    }
