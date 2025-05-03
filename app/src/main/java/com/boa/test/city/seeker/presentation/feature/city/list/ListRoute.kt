package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember


import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun ListRoute(
    coordinator: ListCoordinator = rememberListCoordinator()
) {
    // State observing and declarations
    val uiState by coordinator.screenStateFlow.collectAsStateWithLifecycle(ListState())

    // UI Actions
    val actionsHandler: (ListAction) -> Unit = { action ->
        coordinator.handle(action)
    }

    // UI Rendering
    ListScreen(
        state = uiState,
        onAction = actionsHandler
    )
}
