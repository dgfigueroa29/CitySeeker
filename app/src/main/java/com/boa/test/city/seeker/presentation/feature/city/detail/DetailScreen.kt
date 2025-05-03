package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun DetailScreen(
    state: DetailState,
    onAction: (DetailAction) -> Unit
) {
    // TODO UI Rendering
}

@Composable
@Preview(name = "Detail")
private fun DetailScreenPreview(
    @PreviewParameter(DetailStatePreviewParameterProvider::class)
    state: DetailState
) {
    DetailScreen(
        state = state,
        onAction = {}
    )
}

/**
 * PreviewParameter Provider for DetailScreen Preview
 * Add values to the sequence to see the preview in different states
 **/
class DetailStatePreviewParameterProvider : PreviewParameterProvider<DetailState> {
    override val values: Sequence<DetailState>
        get() = sequenceOf(
            DetailState(),
        )
}
