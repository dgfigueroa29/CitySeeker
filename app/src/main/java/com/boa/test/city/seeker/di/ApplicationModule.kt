package com.boa.test.city.seeker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.boa.test.city.seeker.BuildConfig
import com.boa.test.city.seeker.common.CACHE_SIZE
import com.boa.test.city.seeker.common.FILE_CITY
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.DB_NAME
import com.boa.test.city.seeker.data.network.CityApi
import com.boa.test.city.seeker.data.repository.CityRepositoryImpl
import com.boa.test.city.seeker.data.repository.PreferenceRepositoryImpl
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.data.source.CityDataSourceImpl
import com.boa.test.city.seeker.data.source.PreferenceDataSource
import com.boa.test.city.seeker.data.source.PreferenceDataSourceImpl
import com.boa.test.city.seeker.domain.repository.CityRepository
import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import javax.inject.Singleton

/**
 * Dagger Hilt module that provides application-level dependencies.
 *
 * This module is installed in the [SingletonComponent], meaning that all
 * dependencies provided here will be singletons and available throughout
 * the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    /**
     * Provides a singleton instance of [CityDatabase].
     *
     * @param context The application context.
     * @return A singleton instance of [CityDatabase].
     */
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): CityDatabase =
        Room.databaseBuilder(context, CityDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration(false).build()

    /**
     * Provides an [OkHttpClient] instance configured with logging (in debug builds),
     * Gzip compression, and a 20MB cache.
     *
     * @param context The application context, used to access the cache directory.
     * @return A configured [OkHttpClient] instance.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BASIC
            builder.addInterceptor(logging)
        }

        // Add Gzip compression
        val gzipInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept-Encoding", "gzip")
                .build()
            chain.proceed(request)
        }

        // Cache de 20MB
        val cacheDir = context.cacheDir
        val cache = Cache(File(cacheDir, FILE_CITY), CACHE_SIZE.toLong())
        builder.addInterceptor(gzipInterceptor) // Reduce size in 70% approx
        builder.cache(cache)
        return builder.build()
    }

    /**
     * Provides a Retrofit instance.
     *
     * @param client The OkHttpClient instance.
     * @return A Retrofit instance.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.CITIES_URL)
            .client(client)
            .build()

    /**
     * Provides a singleton instance of [CityApi].
     *
     * This function takes a [Retrofit] instance as a parameter and uses it to create
     * an implementation of the [CityApi] interface. The returned [CityApi] instance
     * is a singleton, meaning that the same instance will be provided every time this
     * function is called.
     *
     * @param retrofit The [Retrofit] instance to use for creating the [CityApi].
     * @return A singleton instance of [CityApi].
     */
    @Provides
    @Singleton
    fun provideCityApi(retrofit: Retrofit): CityApi = retrofit.create(CityApi::class.java)

    /**
     * Provides a singleton instance of [CityDataSource].
     *
     * This function is responsible for creating and providing a [CityDataSource]
     * implementation, which serves as the data source for city-related information.
     * It takes [Context], [CityDatabase], and [CityApi] as dependencies to
     * construct a [CityDataSourceImpl] instance.
     *
     * @param context The application context.
     * @param cityDatabase The Room database instance for local city data.
     * @param cityApi The Retrofit API interface for fetching city data from the network.
     * @return A singleton instance of [CityDataSource].
     */
    @Provides
    @Singleton
    fun provideCityDataSource(
        @ApplicationContext context: Context,
        cityDatabase: CityDatabase,
        cityApi: CityApi
    ): CityDataSource = CityDataSourceImpl(context, cityDatabase, cityApi)

    /**
     * Provides a singleton instance of [CityRepository].
     *
     * This function is responsible for creating and providing a [CityRepository]
     * implementation, which serves as the central point for accessing city-related
     * data. It takes [CityDataSource] and [PreferenceDataSource] as dependencies
     * to construct a [CityRepositoryImpl] instance.
     *
     * @param dataSource The [CityDataSource] to be used by the repository for city data.
     * @param preferenceDataSource The [PreferenceDataSource] to be used by the repository for preference data.
     * @return A singleton instance of [CityRepository].
     */
    @Provides
    @Singleton
    fun provideCityRepository(
        dataSource: CityDataSource,
        preferenceDataSource: PreferenceDataSource
    ): CityRepository =
        CityRepositoryImpl(dataSource, preferenceDataSource)

    /**
     * Provides a singleton instance of [PreferenceRepository].
     *
     * This function is responsible for creating and providing a [PreferenceRepository]
     * implementation. It takes a [PreferenceDataSource] as a dependency
     * to construct a [PreferenceRepositoryImpl] instance.
     *
     * @param preferenceDataSource The [PreferenceDataSource] to be used by the repository for preference data.
     * @return A singleton instance of [PreferenceRepository].
     */
    @Provides
    @Singleton
    fun providePreferenceRepository(preferenceDataSource: PreferenceDataSource): PreferenceRepository =
        PreferenceRepositoryImpl(preferenceDataSource)

    /**
     * Provides a singleton instance of [DataStore] for storing preferences.
     *
     * This DataStore instance is used to persist key-value pairs.
     * The data is stored in a file named "CitySeeker_pref".
     *
     * @param context The application context, used to access the file system.
     * @return A singleton [DataStore<Preferences>] instance.
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("CitySeeker_pref") }
        )

    /**
     * Provides a singleton instance of [PreferenceDataSource].
     *
     * This function is responsible for creating and providing a [PreferenceDataSource]
     * implementation, which serves as the data source for application preferences.
     * It takes a [DataStore<Preferences>] instance as a dependency to
     * construct a [PreferenceDataSourceImpl] instance.
     *
     * @param dataStore The [DataStore<Preferences>] instance for accessing stored preferences.
     * @return A singleton instance of [PreferenceDataSource].
     */
    @Singleton
    @Provides
    fun providePreferenceDataSource(
        dataStore: DataStore<Preferences>
    ): PreferenceDataSource = PreferenceDataSourceImpl(dataStore)
}
