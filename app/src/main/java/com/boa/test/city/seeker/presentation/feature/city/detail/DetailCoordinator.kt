package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class DetailCoordinator(
    val viewModel: DetailViewModel
) {
    val screenStateFlow = viewModel.stateFlow
    fun handle(action: DetailAction) {
        when (action) {
            DetailAction.OnClick -> { /* Handle action */
            }
        }
    }
}

@Composable
fun rememberDetailCoordinator(
    viewModel: DetailViewModel = hiltViewModel()
): DetailCoordinator {
    return remember(viewModel) {
        DetailCoordinator(
            viewModel = viewModel
        )
    }
}
