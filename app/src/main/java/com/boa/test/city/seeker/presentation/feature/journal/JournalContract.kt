package com.boa.test.city.seeker.presentation.feature.journal

import com.boa.test.city.seeker.domain.model.JournalModel

data class JournalState(
    val entries: List<JournalModel> = emptyList(),
    val loadingState: Boolean = true,
)
