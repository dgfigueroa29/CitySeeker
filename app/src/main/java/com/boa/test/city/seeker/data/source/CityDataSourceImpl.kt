package com.boa.test.city.seeker.data.source

import android.content.Context
import com.boa.test.city.seeker.data.local.CityDatabase
import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.data.mapper.CityMapper
import com.boa.test.city.seeker.data.network.CityApi
import com.boa.test.city.seeker.domain.model.CityModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import javax.inject.Inject

class CityDataSourceImpl
    @Inject
    constructor(
        private val context: Context,
        private val cityDatabase: CityDatabase,
        private val cityApi: CityApi,
        private val preferenceDataSource: PreferenceDataSource,
        private val cityMapper: CityMapper,
    ) : CityDataSource {
        private val fileProcessor = CityFileProcessor(cityDatabase)
        private val downloader = CityDownloader(cityApi, context, fileProcessor)
        private val cacheManager = CityCacheManager(context)

        override suspend fun getAllCities(): List<CityEntity> {
            val cities = cityDatabase.cityDao().getAll()
            val (cacheFile, needsDownload) = cacheManager.resolveCacheFile()

            return try {
                if (needsDownload && cities.isEmpty()) {
                    downloader.downloadAndProcess(cacheFile)
                    cacheManager.finalizeCacheFile(cacheFile)
                    cityDatabase.cityDao().getAll()
                } else {
                    cities.ifEmpty {
                        fileProcessor.processFile(cacheFile)
                        cacheManager.finalizeCacheFile(cacheFile)
                        cityDatabase.cityDao().getAll()
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error loading cities: ${e.stackTraceToString()}")
                cities
            }
        }

        override suspend fun searchCities(query: String): List<CityEntity> {
            val cities =
                if (query.isNotEmpty()) {
                    cityDatabase.cityDao().searchCities(query)
                } else {
                    cityDatabase.cityDao().getAll()
                }.distinct()
            return cities
        }

        override fun pagingSource(
            query: String,
            cityMapper: CityMapper,
            favoriteIds: Set<String>,
        ): CityPagingSource =
            CityPagingSource(
                cityDao = cityDatabase.cityDao(),
                query = query,
                cityMapper = cityMapper,
                favoriteIds = favoriteIds,
            )

        override suspend fun mapCities(query: String): List<CityModel> {
            try {
                val favorites = preferenceDataSource.getSetString()
                val cities =
                    cityMapper
                        .mapAll(
                            if (query.isBlank()) getAllCities() else searchCities(query),
                        ).distinct()

                return cities.map { it.copy(isFavorite = favorites.contains(it.id.toString())) }
            } catch (e: Exception) {
                Timber.e("Error mapping cities: ${e.stackTraceToString()}")
                return emptyList()
            }
        }

        override fun getDistinctCountries(): Flow<List<String>> = cityDatabase.cityDao().getDistinctCountries()

        override fun getCitiesByCountry(country: String): Flow<List<CityEntity>> =
            cityDatabase.cityDao().getCitiesByCountry(country)

        override fun getCityById(id: Long): Flow<CityEntity?> =
            cityDatabase
                .cityDao()
                .getCityById(id)
                .catch { e ->
                    Timber.e("Error getting city by id: ${e.stackTraceToString()}")
                    emit(null)
                }
    }
