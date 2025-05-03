package com.boa.test.city.seeker.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boa.test.city.seeker.data.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Query("SELECT * FROM cities WHERE name MATCH :query")
    fun searchCities(query: String): Flow<List<CityEntity>>

    @Query("DELETE FROM cities")
    suspend fun clearAll()
}
