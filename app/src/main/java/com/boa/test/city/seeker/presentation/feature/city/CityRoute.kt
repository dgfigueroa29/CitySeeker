package com.boa.test.city.seeker.presentation.feature.city


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun CityRoute(
    coordinator: CityCoordinator = rememberCityCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(CityState())

    // UI Actions
    val actionsHandler: (CityAction) -> Unit = { action ->
        coordinator.handle(action)
    }

    // UI Rendering
    CityScreen(
        state = uiState,
        onAction = actionsHandler
    )
}
