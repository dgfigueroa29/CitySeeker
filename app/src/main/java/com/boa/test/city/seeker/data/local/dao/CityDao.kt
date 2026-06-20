package com.boa.test.city.seeker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.boa.test.city.seeker.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

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
     *
     * @param query The starting characters of the city name to search for.
     * @return A list of [CityEntity] objects matching the search criteria.
     */
    @Query("SELECT * FROM cities WHERE name LIKE :query || '%' COLLATE NOCASE ORDER BY name, country")
    fun searchCities(query: String): List<CityEntity>

    /**
     * Retrieves all cities from the database, ordered by name and country.
     *
     * @return A list of [CityEntity] objects.
     */
    @Query("SELECT * FROM cities ORDER BY name, country")
    fun getAll(): List<CityEntity>

    /**
     * Retrieves a specific city from the database by its ID as a reactive Flow.
     *
     * @param id The ID of the city to retrieve.
     * @return A [Flow] emitting the [CityEntity] with the specified ID, or `null` if not found.
     * The Flow re-emits whenever the underlying row changes, enabling offline-first reactivity.
     */
    @Query("SELECT * FROM cities WHERE id = :id")
    fun getCityById(id: Long): Flow<CityEntity?>

    /**
     * Retrieves all distinct country names from the database.
     *
     * @return A [Flow] emitting a list of distinct country names, ordered alphabetically.
     */
    @Query("SELECT DISTINCT country FROM cities WHERE country != '' ORDER BY country")
    fun getDistinctCountries(): Flow<List<String>>

    /**
     * Retrieves cities filtered by country name.
     *
     * @param country The country name to filter by.
     * @return A [Flow] emitting a list of [CityEntity] objects in that country, ordered by name.
     */
    @Query("SELECT * FROM cities WHERE country = :country ORDER BY name")
    fun getCitiesByCountry(country: String): Flow<List<CityEntity>>

    /**
     * Retrieves a paginated list of cities matching the search query.
     *
     * @param query The search query to match against city names (case-insensitive).
     * @param limit The maximum number of cities to return.
     * @param offset The number of cities to skip before starting to return results.
     * @return A list of [CityEntity] objects for the requested page.
     */
    @Query(
        "SELECT * FROM cities WHERE name LIKE :query || '%' COLLATE NOCASE ORDER BY name, country LIMIT :limit OFFSET :offset",
    )
    suspend fun searchCitiesPaginated(
        query: String,
        limit: Int,
        offset: Int,
    ): List<CityEntity>

    /**
     * Retrieves a paginated list of all cities.
     *
     * @param limit The maximum number of cities to return.
     * @param offset The number of cities to skip before starting to return results.
     * @return A list of [CityEntity] objects for the requested page.
     */
    @Query("SELECT * FROM cities ORDER BY name, country LIMIT :limit OFFSET :offset")
    suspend fun getAllPaginated(
        limit: Int,
        offset: Int,
    ): List<CityEntity>

    /**
     * Returns the total count of cities matching the search query.
     *
     * @param query The search query to match against city names (case-insensitive).
     * @return The total number of matching cities.
     */
    @Query("SELECT COUNT(*) FROM cities WHERE name LIKE :query || '%' COLLATE NOCASE")
    suspend fun searchCount(query: String): Int

    /**
     * Deletes all cities from the database.
     */
    @Query("DELETE FROM cities")
    suspend fun clearAll()
}
