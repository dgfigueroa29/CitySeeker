package com.boa.test.city.seeker.common.analytics

interface AnalyticsService {
    fun track(event: AnalyticsEvent)

    fun setUserId(userId: String)

    fun setUserProperty(
        key: String,
        value: String,
    )
}
