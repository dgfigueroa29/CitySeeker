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
 * Use case for searching cities based on a text filter.
 *
 * This class is responsible for orchestrating the process of searching for cities
 * by delegating the actual search operation to the [CityRepository]. It handles
 * the flow of data and provides a [UiStateModel] to represent the current state
 * of the search operation (Loading, Success, Error).
 *
 * The search operation is performed on the IO dispatcher to avoid blocking the main thread.
 * It also catches potential exceptions during the search and emits an error state.
 *
 * @property cityRepository The repository responsible for accessing city data.
 */
@Suppress("TooGenericExceptionCaught")
class SearchCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {

    /**
     * Invokes the use case to search for cities.
     *
     * This function takes a text filter and a boolean flag indicating whether to search
     * only for favorite cities. It returns a [Flow] of [UiStateModel] that represents
     * the state of the search operation.
     *
     * The search is performed asynchronously on the IO dispatcher.
     *
     * @param textFilter The text to filter the cities by.
     * @param withOnlyFavorites A boolean flag indicating whether to search only for favorite cities.
     * @return A [Flow] of [UiStateModel] representing the state of the search operation.
     */
    @OptIn(FlowPreview::class)
    operator fun invoke(
        textFilter: String,
        withOnlyFavorites: Boolean
    ): Flow<UiStateModel<List<CityModel>>> = flow {
        emit(UiStateModel.Loading(true))
        val cities = cityRepository.searchCities(textFilter, withOnlyFavorites)
        emit(UiStateModel.Success(cities))
    }.catch {
        Timber.e("Error in flow searching cities: ${it.stackTraceToString()}")
        emit(UiStateModel.Error(it.message ?: "An unknown error occurred"))
    }.flowOn(Dispatchers.IO)
}
