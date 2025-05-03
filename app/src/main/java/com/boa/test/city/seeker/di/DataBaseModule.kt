package com.boa.test.city.seeker.di

import android.content.Context
import androidx.room.Room
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.CityDatabase.Companion.DB_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataBaseModule {
    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): CityDatabase {
        return Room.databaseBuilder(context, CityDatabase::class.java, DB_NAME).build()
    }
}