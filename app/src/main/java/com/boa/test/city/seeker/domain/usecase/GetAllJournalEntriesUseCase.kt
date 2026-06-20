package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.model.JournalModel
import com.boa.test.city.seeker.domain.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAllJournalEntriesUseCase
@Inject
constructor(
    private val journalRepository: JournalRepository,
) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<List<JournalModel>> =
        if (forceRefresh) {
            journalRepository.getAllEntries().flowOn(Dispatchers.IO)
        } else {
            journalRepository.getAllEntries().flowOn(Dispatchers.IO).distinctUntilChanged()
        }
}
