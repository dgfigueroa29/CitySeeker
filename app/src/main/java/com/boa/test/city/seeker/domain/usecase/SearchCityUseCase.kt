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

@Suppress("TooGenericExceptionCaught")
class SearchCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
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
        Timber.e("Error in flow: ${it.stackTraceToString()}")
        emit(UiStateModel.Error(it.message ?: "An unknown error occurred"))
    }.flowOn(Dispatchers.IO)
}
