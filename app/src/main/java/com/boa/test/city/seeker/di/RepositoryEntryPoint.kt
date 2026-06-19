package com.boa.test.city.seeker.di

import com.boa.test.city.seeker.domain.repository.CityRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryEntryPoint {
    fun cityRepository(): CityRepository
}
