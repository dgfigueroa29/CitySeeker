package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen's coordinator which is responsible for handling actions from the UI layer
 * and one-shot actions based on the new UI state
 */
class ListCoordinator(
    val viewModel: ListViewModel
) {
    val screenStateFlow = viewModel.stateFlow
    fun handle(action: ListAction) {
        when (action) {
            ListAction.OnClick -> { /* Handle action */
            }
        }
    }


}

@Composable
fun rememberListCoordinator(
    viewModel: ListViewModel = hiltViewModel()
): ListCoordinator {
    return remember(viewModel) {
        ListCoordinator(
            viewModel = viewModel
        )
    }
}
