package com.boa.test.city.seeker.domain.repository

import androidx.paging.PagingData
import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.Flow

interface CityRepository {
    suspend fun searchCities(query: String): Flow<PagingData<CityModel>>
}