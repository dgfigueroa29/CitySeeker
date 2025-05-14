package com.boa.test.city.seeker.domain.usecase

import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case to retrieve a city by its unique identifier.
 *
 * This use case interacts with the [CityRepository] to fetch city data based on the provided ID.
 * It emits the current UI state ([UiStateModel]) during the data fetching process,
 * including loading, success, and error states.
 *
 * @property cityRepository The repository responsible for city data operations.
 */
@Suppress("TooGenericExceptionCaught")
class GetCityByIdUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    /**
     * Invokes the use case to retrieve a city by its ID.
     *
     * This function emits [UiStateModel] updates to reflect the loading state, success, or error
     * during the process of fetching the city data.
     *
     * @param id The ID of the city to retrieve.
     * @return A [Flow] of [UiStateModel] containing the [CityModel] if successful, or an error message.
     */
    @OptIn(FlowPreview::class)
    operator fun invoke(id: Long): Flow<UiStateModel<CityModel>> = flow {
        emit(UiStateModel.Loading(true))
        cityRepository.getCityById(id).collect {
            try {
                emit(UiStateModel.Success(it))
            } catch (e: Exception) {
                Timber.e("Error getting city by id: ${e.stackTraceToString()}")
                emit(UiStateModel.Error(e.message ?: "An unknown error occurred"))
            }
            emit(UiStateModel.Loading(false))
        }
    }.catch {
        Timber.e("Error in flow getting city by id: ${it.stackTraceToString()}")
        emit(UiStateModel.Error(it.message ?: "An unknown error occurred"))
    }.flowOn(Dispatchers.IO)
}
