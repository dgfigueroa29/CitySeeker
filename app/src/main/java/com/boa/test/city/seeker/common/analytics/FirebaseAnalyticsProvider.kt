package com.boa.test.city.seeker.common.analytics

import android.app.Application
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsProvider
@Inject
constructor(
    private val application: Application,
) : AnalyticsService {
    companion object {
        private const val TAG = "FirebaseAnalytics"
    }

    private val firebaseAnalytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseAnalytics.getInstance(application)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Firebase not initialized — missing google-services.json?")
            null
        }
    }
    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseCrashlytics.getInstance()
        } catch (e: IllegalStateException) {
            null
        }
    }

    override fun track(event: AnalyticsEvent) {
        when (event) {
            is AnalyticsEvent.Error.Occurred -> crashlytics?.recordException(event.throwable)
            is AnalyticsEvent.Performance.Trace -> {}
            else -> {
                val fa = firebaseAnalytics ?: return
                val bundle =
                    android.os.Bundle().apply {
                        event.properties.forEach { (key, value) ->
                            when (value) {
                                is String -> putString(key, value)
                                is Int -> putInt(key, value)
                                is Long -> putLong(key, value)
                                is Boolean -> putBoolean(key, value)
                                is Double -> putDouble(key, value)
                            }
                        }
                    }
                fa.logEvent(event.name, bundle)
            }
        }
    }

    override fun setUserId(userId: String) {
        firebaseAnalytics?.setUserId(userId)
        crashlytics?.setUserId(userId)
    }

    override fun setUserProperty(
        key: String,
        value: String,
    ) {
        firebaseAnalytics?.setUserProperty(key, value)
    }
}
