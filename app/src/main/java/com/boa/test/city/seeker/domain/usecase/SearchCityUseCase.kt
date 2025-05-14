package com.boa.test.city.seeker.domain.usecase

import androidx.paging.PagingData
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
 * Use case for searching cities based on a text filter.
 *
 * This class is responsible for orchestrating the process of searching for cities
 * by delegating the actual search operation to the [CityRepository]. It handles
 * the flow of data and provides a [UiStateModel] to represent the current state
 * of the search operation (Loading, Success, Error).
 *
 * The search operation is performed on the IO dispatcher to avoid blocking the main thread.
 * It also catches potential exceptions during the search and emits an error state.
 */
@Suppress("TooGenericExceptionCaught")
class SearchCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    /**
     * Invokes the search city use case.
     *
     * This function initiates a search for cities based on the provided text filter.
     * It emits [UiStateModel] events to represent the state of the search operation,
     * including loading, success with paginated city data, and errors.
     *
     * The search is performed asynchronously on the IO dispatcher.
     *
     * @param textFilter The text to filter cities by.
     * @return A [Flow] of [UiStateModel] representing the state of the search,
     *   containing [PagingData] of [CityModel] on success.
     */
    @OptIn(FlowPreview::class)
    operator fun invoke(textFilter: String): Flow<UiStateModel<PagingData<CityModel>>> = flow {
        emit(UiStateModel.Loading(true))
        cityRepository.searchCities(textFilter).collect {
            try {
                emit(UiStateModel.Success(it))
            } catch (e: Exception) {
                Timber.e("Error searching cities: ${e.stackTraceToString()}")
                emit(UiStateModel.Error(e.message ?: "An unknown error occurred"))
            }
            emit(UiStateModel.Loading(false))
        }
    }.catch {
        Timber.e("Error in flow searching cities: ${it.stackTraceToString()}")
        emit(UiStateModel.Error(it.message ?: "An unknown error occurred"))
    }.flowOn(Dispatchers.IO)
}
