package com.boa.test.city.seeker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.local.entity.CityEntity

@Dao
interface CityDao {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' COLLATE NOCASE ORDER BY name, country LIMIT :limit")
    fun searchCities(query: String, limit: Int = LIMIT): List<CityEntity>

    @Query("SELECT * FROM cities ORDER BY name, country LIMIT :limit")
    fun getAll(limit: Int = LIMIT): List<CityEntity>

    @Query("DELETE FROM cities")
    suspend fun clearAll()
}
