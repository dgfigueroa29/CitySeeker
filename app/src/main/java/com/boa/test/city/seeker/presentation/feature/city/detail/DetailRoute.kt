package com.boa.test.city.seeker.presentation.feature.city.detail


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun DetailRoute(
    coordinator: DetailCoordinator = rememberDetailCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(DetailState())

    // UI Actions
    val actionsHandler: (DetailAction) -> Unit = { action ->
        coordinator.handle(action)
    }

    // UI Rendering
    DetailScreen(
        state = uiState,
        onAction = actionsHandler
    )
}
