package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCountriesUseCase
@Inject
constructor(
    private val cityRepository: CityRepository,
) {
    operator fun invoke(): Flow<List<String>> =
        cityRepository.getDistinctCountries().flowOn(Dispatchers.IO)
}
