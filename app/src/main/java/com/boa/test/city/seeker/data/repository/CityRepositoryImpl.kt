package com.boa.test.city.seeker.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.boa.test.city.seeker.common.LIMIT
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.source.CityDataSource
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class CityRepositoryImpl(
    private val cityDataSource: CityDataSource,
    private val preferenceRepository: PreferenceRepository,
    private val cityMapper: CityMapper,
) : CityRepository {

    override suspend fun searchCitiesAndPaginate(query: String): Flow<PagingData<CityModel>> {
        try {
            val favorites = preferenceRepository.getSetString()
            val source = cityDataSource.pagingSource(query, cityMapper, favorites)
            return Pager(
                config = PagingConfig(pageSize = LIMIT, enablePlaceholders = false),
                pagingSourceFactory = { source },
            ).flow
        } catch (e: Exception) {
            Timber.e("Error creating paging flow: ${e.stackTraceToString()}")
            return flowOf(PagingData.empty<CityModel>())
        }
    }

    override suspend fun searchCities(
        query: String,
        withOnlyFavorites: Boolean,
    ): List<CityModel> {
        try {
            val favorites = preferenceRepository.getSetString()
            val cities =
                cityDataSource
                    .mapCities(query)
                    .map { it.copy(isFavorite = favorites.contains(it.id.toString())) }
            return if (withOnlyFavorites) {
                cities.filter { it.isFavorite }
            } else {
                cities
            }
        } catch (e: Exception) {
            Timber.e("Error searchCities: ${e.stackTraceToString()}")
            return emptyList()
        }
    }

    override fun getCityById(id: Long): Flow<CityModel> =
        flow {
            cityDataSource.getCityById(id).collect { entity ->
                if (entity != null) {
                    val isFavorite = preferenceRepository.hasString(entity.id.toString())
                    val model = cityMapper.map(entity).copy(isFavorite = isFavorite)
                    emit(model)
                } else {
                    emit(CityModel())
                }
            }
        }.catch { e ->
            Timber.e("Error getCityById: ${e.stackTraceToString()}")
            emit(CityModel())
        }

    override fun getDistinctCountries(): Flow<List<String>> =
        cityDataSource.getDistinctCountries()

    override fun getCitiesByCountry(country: String): Flow<List<CityModel>> =
        flow {
            cityDataSource.getCitiesByCountry(country).collect { entities ->
                val favorites = preferenceRepository.getSetString()
                val models = entities.map { entity ->
                    cityMapper.map(entity).copy(isFavorite = favorites.contains(entity.id.toString()))
                }
                emit(models)
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun getRecommendations(limit: Int): List<CityModel> {
        try {
            val favorites = preferenceRepository.getSetString()
            val searchHistory = preferenceRepository.getSearchHistory()
            val allCities = cityMapper.mapAll(cityDataSource.getAllCities()).map {
                it.copy(isFavorite = favorites.contains(it.id.toString()))
            }

            val ranked = allCities.map { city ->
                var score = 0
                if (city.isFavorite) score += 10
                if (searchHistory.any { city.name.contains(it, ignoreCase = true) }) score += 5
                if (searchHistory.any { city.country.contains(it, ignoreCase = true) }) score += 3
                city to score
            }
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first }

            return ranked
        } catch (e: Exception) {
            Timber.e("Error getRecommendations: ${e.stackTraceToString()}")
            return emptyList()
        }
    }
}
