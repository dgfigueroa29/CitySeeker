package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.entity.CityEntity

interface CityDataSource {
    suspend fun getAllCities(): List<CityEntity>
    suspend fun searchCities(query: String): List<CityEntity>
}