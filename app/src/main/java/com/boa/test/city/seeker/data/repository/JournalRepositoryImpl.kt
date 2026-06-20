package com.boa.test.city.seeker.data.repository

import com.boa.test.city.seeker.data.local.entity.CityEntity
import com.boa.test.city.seeker.data.local.entity.JournalEntity
import com.boa.test.city.seeker.data.source.JournalDataSource
import com.boa.test.city.seeker.domain.model.JournalModel
import com.boa.test.city.seeker.domain.repository.JournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

class JournalRepositoryImpl
@Inject
constructor(
    private val journalDataSource: JournalDataSource,
    private val cityDataSource: com.boa.test.city.seeker.data.source.CityDataSource,
) : JournalRepository {
    private val cityNameCache = mutableMapOf<Long, String>()

    override suspend fun addEntry(
        cityId: Long,
        title: String,
        notes: String,
        rating: Int,
        photoUri: String?,
    ) {
        journalDataSource.insertEntry(
            JournalEntity(
                cityId = cityId,
                title = title,
                notes = notes,
                rating = rating,
                photoUri = photoUri,
            ),
        )
        cityNameCache.remove(cityId)
    }

    override suspend fun deleteEntry(entryId: Long) {
        val entry = journalDataSource.getEntryById(entryId)
        if (entry != null) {
            journalDataSource.deleteEntry(entry)
        }
    }

    override fun getEntriesForCity(cityId: Long): Flow<List<JournalModel>> =
        flow {
            journalDataSource.getEntriesForCity(cityId).collect { entities ->
                val models = entities.map { entity ->
                    val cityName = resolveCityName(entity.cityId)
                    entity.toModel(cityName)
                }
                emit(models)
            }
        }.catch { e ->
            Timber.e("Error loading journal entries: ${e.stackTraceToString()}")
            emit(emptyList())
        }.flowOn(Dispatchers.IO)

    override fun getAllEntries(): Flow<List<JournalModel>> =
        flow {
            journalDataSource.getAllEntries().collect { entities ->
                val models = entities.map { entity ->
                    val cityName = resolveCityName(entity.cityId)
                    entity.toModel(cityName)
                }
                emit(models)
            }
        }.catch { e ->
            Timber.e("Error loading all journal entries: ${e.stackTraceToString()}")
            emit(emptyList())
        }.flowOn(Dispatchers.IO)

    private suspend fun resolveCityName(cityId: Long): String {
        cityNameCache[cityId]?.let { return it }
        val name = resolveFromDatabase(cityId)
        cityNameCache[cityId] = name
        return name
    }

    private suspend fun resolveFromDatabase(cityId: Long): String =
        try {
            val entities = cityDataSource.getAllCities()
            entities.firstOrNull { it.id == cityId }?.name ?: ""
        } catch (e: Exception) {
            Timber.e("Error resolving city name: ${e.stackTraceToString()}")
            ""
        }
}

private fun JournalEntity.toModel(cityName: String) =
    JournalModel(
        id = id,
        cityId = cityId,
        cityName = cityName,
        title = title,
        notes = notes,
        rating = rating,
        photoUri = photoUri,
        visitDate = visitDate,
        createdAt = createdAt,
    )
