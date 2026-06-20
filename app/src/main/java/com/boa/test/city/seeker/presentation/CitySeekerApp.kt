package com.boa.test.city.seeker.presentation

import android.app.Application
import com.boa.test.city.seeker.BuildConfig
import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.map.MapCacheManager
import com.boa.test.city.seeker.common.map.OfflineTileManager
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class CitySeekerApp : Application() {
    @Inject
    lateinit var analyticsService: AnalyticsService

    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_TOKEN

        MapCacheManager.init(this)
        OfflineTileManager.init(this)

        initSentry()
        initTimber()
    }

    private fun initSentry() {
        val dsn = BuildConfig.SENTRY_DSN
        if (dsn.isNotBlank()) {
            SentryAndroid.init(this) { options ->
                options.dsn = dsn
                options.tracesSampleRate = if (BuildConfig.DEBUG) 1.0 else 0.2
                options.isEnableUserInteractionTracing = true
                options.environment = if (BuildConfig.DEBUG) "development" else "production"
                options.release = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                options.isEnableAutoSessionTracking = true
                options.sessionTrackingIntervalMillis = 30_000
            }
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
