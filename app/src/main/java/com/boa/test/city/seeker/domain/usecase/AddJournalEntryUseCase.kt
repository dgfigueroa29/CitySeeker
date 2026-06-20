package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.JournalRepository
import javax.inject.Inject

class AddJournalEntryUseCase
@Inject
constructor(
    private val journalRepository: JournalRepository,
) {
    suspend operator fun invoke(
        cityId: Long,
        title: String,
        notes: String,
        rating: Int = 0,
        photoUri: String? = null,
    ) {
        journalRepository.addEntry(cityId, title, notes, rating, photoUri)
    }
}
