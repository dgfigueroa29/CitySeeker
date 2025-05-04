package com.boa.test.city.seeker.di

import android.content.Context
import androidx.room.Room
import com.boa.test.city.seeker.BuildConfig
import com.boa.test.city.seeker.common.CACHE_SIZE
import com.boa.test.city.seeker.common.FILE_CITY
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.DB_NAME
import com.boa.test.city.seeker.data.network.CityApi
import com.boa.test.city.seeker.data.repository.CityRepositoryImpl
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.data.source.CityDataSourceImpl
import com.boa.test.city.seeker.domain.repository.CityRepository
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

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): CityDatabase {
        return Room.databaseBuilder(context, CityDatabase::class.java, DB_NAME)
            .fallbackToDestructiveMigration(false).build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logging)
        }

        // Add Gzip compression
        val gzipInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept-Encoding", "gzip")
                .build()
            chain.proceed(request)
        }

        // Cache de 10MB
        val cache = Cache(File(context.cacheDir, FILE_CITY), CACHE_SIZE.toLong())
        builder.addInterceptor(gzipInterceptor) // Reduce size in 70% approx
        builder.cache(cache)
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
    ): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.CITIES_URL)
            //.addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideCityApi(retrofit: Retrofit): CityApi {
        return retrofit.create(CityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCityDataSource(
        @ApplicationContext context: Context,
        cityDatabase: CityDatabase,
        cityApi: CityApi
    ): CityDataSource = CityDataSourceImpl(context, cityDatabase, cityApi)

    @Provides
    @Singleton
    fun provideCityRepository(dataSource: CityDataSource): CityRepository =
        CityRepositoryImpl(dataSource)
}
