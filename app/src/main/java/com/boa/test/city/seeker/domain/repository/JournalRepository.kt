package com.boa.test.city.seeker.domain.repository

import com.boa.test.city.seeker.domain.model.JournalModel
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    suspend fun addEntry(
        cityId: Long,
        title: String,
        notes: String,
        rating: Int,
        photoUri: String? = null,
    )

    suspend fun deleteEntry(entryId: Long)

    fun getEntriesForCity(cityId: Long): Flow<List<JournalModel>>

    fun getAllEntries(): Flow<List<JournalModel>>
}
