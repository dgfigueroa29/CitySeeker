package com.boa.test.city.seeker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.data.source.OptimizedCityTrie
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class CityRepositoryImpl(
    private val cityDataSource: CityDataSource,
    private val preferenceRepository: PreferenceRepository,
    private val cityMapper: CityMapper,
) : CityRepository {
    private val trie = OptimizedCityTrie()
    private var isInitialized = false

    /**
     * Initializes the Trie data structure with all cities from the data source.
     *
     * This function retrieves all cities from [cityDataSource], maps them to [CityModel] objects,
     * and inserts them into the [trie]. It also checks if each city is marked as a favorite
     * using [preferenceRepository] and updates the `isFavorite` property of the [CityModel] accordingly.
     *
     * This function is idempotent; it will only initialize the Trie once.
     * If an error occurs during initialization, it is logged using Timber.
     */
    suspend fun initializeTrie() {
        try {
            if (isInitialized) return
            val favorites = preferenceRepository.getSetString()
            val cities = cityMapper.mapAll(cityDataSource.getAllCities())
            cities.forEach {
                trie.insert(it.copy(isFavorite = favorites.contains(it.id.toString())))
            }

            isInitialized = true
        } catch (e: Exception) {
            Timber.e("Error initializeTrie: ${e.stackTraceToString()}")
        }
    }

    /**
     * Searches for cities based on the provided query.
     *
     * This function utilizes a PagingSource to efficiently load city data in chunks.
     * If the query is blank, it triggers the initialization of the Trie data structure,
     * which is used for prefix-based searching.
     *
     * @param query The search query string.
     * @return A [Flow] of [PagingData] containing [CityModel] objects that match the query.
     * Returns an empty PagingData flow in case of an error.
     */
    override suspend fun searchCitiesAndPaginate(query: String): Flow<PagingData<CityModel>> {
        // This function will be fixed in the next version when
        // we refactoring the code for applying cache from ViewModel
        try {
            val pagingSource =
                cityDataSource.pagingSource(query, trie)
            if (query.isBlank()) {
                initializeTrie()
            }
            return Pager(
                config = PagingConfig(pageSize = LIMIT),
                pagingSourceFactory = { pagingSource },
            ).flow
        } catch (e: Exception) {
            Timber.e("Error initializeTrie: ${e.stackTraceToString()}")
            return flow { PagingData.Companion.empty<CityModel>() }
        }
    }

    /**
     * Searches for cities based on the provided query and optionally filters by favorites.
     *
     * If the `query` is blank, this function ensures the Trie data structure is initialized
     * by calling [initializeTrie]. This Trie is then used by [CityDataSource.mapCities]
     * for efficient prefix-based searching.
     *
     * After retrieving the initial list of cities matching the `query`, it updates the
     * `isFavorite` status of each [CityModel] based on the user's preferences stored
     * via [preferenceRepository].
     *
     * If `withOnlyFavorites` is true, the resulting list is further filtered to include
     * only cities marked as favorites.
     *
     * @param query The search query string. Can be blank to fetch initial cities.
     * @param withOnlyFavorites A boolean flag indicating whether to return only favorite cities.
     * @return A list of [CityModel] objects that match the query and the favorite filter.
     * Returns an empty list if an error occurs during the search process or Trie initialization.
     */
    override suspend fun searchCities(
        query: String,
        withOnlyFavorites: Boolean,
    ): List<CityModel> {
        try {
            if (query.isBlank()) {
                initializeTrie()
            }

            val favorites = preferenceRepository.getSetString()
            val cities =
                cityDataSource
                    .mapCities(query, trie)
                    .map { it.copy(isFavorite = favorites.contains(it.id.toString())) }
            return if (withOnlyFavorites) {
                cities.filter { it.isFavorite }
            } else {
                cities
            }
        } catch (e: Exception) {
            Timber.e("Error initializeTrie: ${e.stackTraceToString()}")
            return emptyList()
        }
    }

    /**
     * Retrieves a city by its unique identifier.
     *
     * This function fetches a city from the [cityDataSource] using the provided ID.
     * It then maps the raw city data to a [CityModel] and checks if the city is marked as a favorite
     * using the [preferenceRepository].
     *
     * @param id The unique identifier of the city.
     * @return A [Flow] emitting a [CityModel] corresponding to the given ID.
     * If no city is found with the given ID, or if an error occurs during the process,
     * an empty [CityModel] is emitted.
     */
    override fun getCityById(id: Long): Flow<CityModel> =
        flow {
            try {
                val city = cityDataSource.getCityById(id)
                if (city != null) {
                    val isFavorite = preferenceRepository.hasString(city.id.toString())
                    val model = cityMapper.map(city).copy(isFavorite = isFavorite)
                    emit(model)
                } else {
                    emit(CityModel())
                }
            } catch (e: Exception) {
                Timber.e("Error getCityById: ${e.stackTraceToString()}")
                emit(CityModel())
            }
        }
}
