package com.boa.test.city.seeker.presentation.feature.city

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class CityCoordinator(
    val viewModel: CityViewModel
) {
    val screenStateFlow = viewModel.stateFlow
    fun handle(action: CityAction) {
        when (action) {
            CityAction.OnClick -> { }
        }
    }
}

@Composable
fun rememberCityCoordinator(
    viewModel: CityViewModel = hiltViewModel()
): CityCoordinator {
    return remember(viewModel) {
        CityCoordinator(
            viewModel = viewModel
        )
    }
}
