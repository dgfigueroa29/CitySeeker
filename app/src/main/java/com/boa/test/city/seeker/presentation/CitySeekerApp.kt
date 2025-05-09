package com.boa.test.city.seeker.presentation

import android.app.Application
import com.boa.test.city.seeker.BuildConfig
import com.mapbox.common.MapboxOptions
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CitySeekerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_TOKEN

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
