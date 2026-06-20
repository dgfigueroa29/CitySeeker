package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import javax.inject.Inject

class RecordSearchUseCase
@Inject
constructor(
    private val preferenceRepository: PreferenceRepository,
) {
    suspend operator fun invoke(query: String) {
        if (query.isNotBlank()) {
            preferenceRepository.addSearchQuery(query.trim())
        }
    }
}
