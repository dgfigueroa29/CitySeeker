package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

interface JournalDataSource {
    suspend fun insertEntry(entry: JournalEntity)

    suspend fun deleteEntry(entry: JournalEntity)

    fun getEntriesForCity(cityId: Long): Flow<List<JournalEntity>>

    fun getAllEntries(): Flow<List<JournalEntity>>

    suspend fun getEntryById(id: Long): JournalEntity?

    suspend fun deleteEntriesForCity(cityId: Long)
}
