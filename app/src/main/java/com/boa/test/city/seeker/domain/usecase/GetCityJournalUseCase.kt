package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.model.JournalModel
import com.boa.test.city.seeker.domain.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCityJournalUseCase
    @Inject
    constructor(
        private val journalRepository: JournalRepository,
    ) {
        operator fun invoke(cityId: Long): Flow<List<JournalModel>> =
            journalRepository.getEntriesForCity(cityId).flowOn(Dispatchers.IO)
    }
