package com.boa.test.city.seeker.common.di

import com.boa.test.city.seeker.common.analytics.AnalyticsService
import com.boa.test.city.seeker.common.analytics.CompositeAnalyticsService
import com.boa.test.city.seeker.common.analytics.FirebaseAnalyticsProvider
import com.boa.test.city.seeker.common.analytics.SentryAnalyticsProvider
import com.boa.test.city.seeker.common.analytics.TimberAnalyticsProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideAnalyticsService(
        timberProvider: TimberAnalyticsProvider,
        sentryProvider: SentryAnalyticsProvider,
        firebaseProvider: FirebaseAnalyticsProvider,
    ): AnalyticsService {
        val providers = mutableSetOf<AnalyticsService>(timberProvider, sentryProvider, firebaseProvider)
        return CompositeAnalyticsService(providers)
    }
}
