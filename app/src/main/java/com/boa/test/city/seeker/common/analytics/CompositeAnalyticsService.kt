package com.boa.test.city.seeker.common.analytics

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeAnalyticsService
@Inject
constructor(
    private val providers: Set<@JvmSuppressWildcards AnalyticsService>,
) : AnalyticsService {
    override fun track(event: AnalyticsEvent) {
        providers.forEach { provider ->
            runCatching { provider.track(event) }
                .onFailure { Timber.tag(TAG).w(it, "Provider %s failed", provider.javaClass.simpleName) }
        }
    }

    override fun setUserId(userId: String) {
        providers.forEach { provider ->
            runCatching { provider.setUserId(userId) }
                .onFailure { Timber.tag(TAG).w(it, "Provider %s setUserId failed", provider.javaClass.simpleName) }
        }
    }

    override fun setUserProperty(
        key: String,
        value: String,
    ) {
        providers.forEach { provider ->
            runCatching { provider.setUserProperty(key, value) }
                .onFailure {
                    Timber
                        .tag(
                            TAG,
                        ).w(it, "Provider %s setUserProperty failed", provider.javaClass.simpleName)
                }
        }
    }

    companion object {
        private const val TAG = "CompositeAnalytics"
    }
}
