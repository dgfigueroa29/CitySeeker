package com.boa.test.city.seeker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.local.entity.CityEntity

/**
 * Data Access Object (DAO) for the City entity.
 *
 * Provides methods for interacting with the `cities` table in the database.
 */
@Dao
interface CityDao {
    /**
     * Inserts a list of City entities into the database.
     * If a city with the same primary key already exists, it will be replaced.
     *
     * @param cities The list of [CityEntity] objects to insert.
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    /**
     * Searches for cities in the database whose names start with the given query.
     *
     * The search is case-insensitive and results are ordered by name and then country.
     * The number of results is limited by the [limit] parameter.
     *
     * @param query The starting characters of the city name to search for.
     * @param limit The maximum number of results to return. Defaults to [LIMIT].
     * @return A list of [CityEntity] objects matching the search criteria.
     */
    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' COLLATE NOCASE ORDER BY name, country LIMIT :limit")
    fun searchCities(query: String, limit: Int = LIMIT): List<CityEntity>

    /**
     * Retrieves all cities from the database, ordered by name and country.
     *
     * @param limit The maximum number of cities to retrieve. Defaults to [LIMIT].
     * @return A list of [CityEntity] objects.
     */
    @Query("SELECT * FROM cities ORDER BY name, country LIMIT :limit")
    fun getAll(limit: Int = LIMIT): List<CityEntity>

    /**
     * Retrieves a specific city from the database by its ID.
     *
     * @param id The ID of the city to retrieve.
     * @return The [CityEntity] with the specified ID, or `null` if not found.
     */
    @Query("SELECT * FROM cities WHERE id = :id")
    fun getCityById(id: Long): CityEntity?

    /**
     * Deletes all cities from the database.
     */
    @Query("DELETE FROM cities")
    suspend fun clearAll()
}
