package com.boa.test.city.seeker.domain.usecase

import androidx.paging.PagingData
import com.boa.test.city.seeker.domain.model.CityModel
import com.boa.test.city.seeker.domain.model.UiStateModel
import com.boa.test.city.seeker.domain.repository.CityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Suppress("TooGenericExceptionCaught")
class SearchCityUseCase @Inject constructor(
    private val cityRepository: CityRepository
) {
    private val debouncePeriod = 300L

    @OptIn(FlowPreview::class)
    operator fun invoke(textFilter: String): Flow<UiStateModel<PagingData<CityModel>>> = flow {
        //Connecting with the repository in Data Layer
        emit(UiStateModel.Loading(true))
        cityRepository.searchCities(textFilter).collect {
            try {
                emit(UiStateModel.Success(it))
            } catch (e: Exception) {
                emit(UiStateModel.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }.flowOn(Dispatchers.Default)
        .debounce(debouncePeriod)
}
