package com.boa.test.city.seeker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boa.test.city.seeker.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntity)

    @Delete
    suspend fun deleteEntry(entry: JournalEntity)

    @Query("SELECT * FROM journal_entries WHERE city_id = :cityId ORDER BY visit_date DESC")
    fun getEntriesForCity(cityId: Long): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY visit_date DESC")
    fun getAllEntries(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntity?

    @Query("DELETE FROM journal_entries WHERE city_id = :cityId")
    suspend fun deleteEntriesForCity(cityId: Long)
}
