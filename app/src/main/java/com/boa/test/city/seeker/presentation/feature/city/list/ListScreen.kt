package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun ListScreen(
    state: ListState,
    onAction: (ListAction) -> Unit
) {
    // TODO UI Rendering
}

@Composable
@Preview(name = "List")
private fun ListScreenPreview(
    @PreviewParameter(ListStatePreviewParameterProvider::class)
    state: ListState
) {
    ListScreen(
        state = state,
        onAction = {}
    )
}

/**
 * PreviewParameter Provider for ListScreen Preview
 * Add values to the sequence to see the preview in different states
 **/
class ListStatePreviewParameterProvider : PreviewParameterProvider<ListState> {
    override val values: Sequence<ListState>
        get() = sequenceOf(
            ListState(),
        )
}
