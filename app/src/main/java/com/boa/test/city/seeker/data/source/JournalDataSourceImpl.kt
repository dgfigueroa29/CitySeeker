package com.boa.test.city.seeker.data.source

import com.boa.test.city.seeker.data.local.dao.JournalDao
import com.boa.test.city.seeker.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class JournalDataSourceImpl
    @Inject
    constructor(
        private val journalDao: JournalDao,
    ) : JournalDataSource {
        override suspend fun insertEntry(entry: JournalEntity) {
            journalDao.insertEntry(entry)
        }

        override suspend fun deleteEntry(entry: JournalEntity) {
            journalDao.deleteEntry(entry)
        }

        override fun getEntriesForCity(cityId: Long): Flow<List<JournalEntity>> = journalDao.getEntriesForCity(cityId)

        override fun getAllEntries(): Flow<List<JournalEntity>> = journalDao.getAllEntries()

        override suspend fun getEntryById(id: Long): JournalEntity? = journalDao.getEntryById(id)

        override suspend fun deleteEntriesForCity(cityId: Long) {
            journalDao.deleteEntriesForCity(cityId)
        }
    }
