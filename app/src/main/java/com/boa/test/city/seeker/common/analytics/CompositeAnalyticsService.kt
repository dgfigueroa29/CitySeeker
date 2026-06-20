package com.boa.test.city.seeker.common.analytics

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositeAnalyticsService
@Inject
constructor(
    private val providers: Set<@JvmSuppressWildcards AnalyticsService>,
) : AnalyticsService {
    private val consentGranted = AtomicBoolean(false)

    override fun track(event: AnalyticsEvent) {
        if (!consentGranted.get() && event !is AnalyticsEvent.Error) return
        providers.forEach { provider ->
            runCatching { provider.track(event) }
                .onFailure { Timber.tag(TAG).w(it, "Provider %s failed", provider.javaClass.simpleName) }
        }
    }

    override fun setUserId(userId: String) {
        if (!consentGranted.get()) return
        providers.forEach { provider ->
            runCatching { provider.setUserId(userId) }
                .onFailure { Timber.tag(TAG).w(it, "Provider %s setUserId failed", provider.javaClass.simpleName) }
        }
    }

    override fun setUserProperty(
        key: String,
        value: String,
    ) {
        if (!consentGranted.get()) return
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

    override fun setConsentGranted(granted: Boolean) {
        consentGranted.set(granted)
    }

    companion object {
        private const val TAG = "CompositeAnalytics"
    }
}
