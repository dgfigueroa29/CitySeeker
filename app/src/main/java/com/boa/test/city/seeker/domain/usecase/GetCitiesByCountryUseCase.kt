package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCitiesByCountryUseCase
    @Inject
    constructor(
        private val cityRepository: CityRepository,
    ) {
        operator fun invoke(country: String): Flow<List<CityModel>> =
            cityRepository.getCitiesByCountry(country).flowOn(Dispatchers.IO)
    }
