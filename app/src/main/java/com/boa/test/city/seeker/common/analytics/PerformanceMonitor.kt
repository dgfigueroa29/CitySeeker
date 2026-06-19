package com.boa.test.city.seeker.common.analytics

import android.os.SystemClock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitor
@Inject
constructor(
    private val analyticsService: AnalyticsService,
) {
    private val activeTraces = mutableMapOf<String, Long>()

    fun start(operation: String) {
        activeTraces[operation] = SystemClock.elapsedRealtime()
        Timber.tag(TAG).d("[START] %s", operation)
    }

    fun stop(operation: String) {
        val startTime =
            activeTraces.remove(operation)
                ?: run {
                    Timber.tag(TAG).w("No active trace: %s", operation)
                    return
                }
        val durationMs = SystemClock.elapsedRealtime() - startTime
        Timber.tag(TAG).d("[STOP] %s = %dms", operation, durationMs)
        analyticsService.track(AnalyticsEvent.Performance.Trace(operation, durationMs))
    }

    fun <T> trace(
        operation: String,
        block: () -> T,
    ): T {
        start(operation)
        return try {
            block()
        } finally {
            stop(operation)
        }
    }

    companion object {
        private const val TAG = "Performance"
    }
}
