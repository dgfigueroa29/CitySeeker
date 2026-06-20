package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.JournalRepository
import javax.inject.Inject

class DeleteJournalEntryUseCase
    @Inject
    constructor(
        private val journalRepository: JournalRepository,
    ) {
        suspend operator fun invoke(entryId: Long) {
            journalRepository.deleteEntry(entryId)
        }
    }
