package com.boa.test.city.seeker.presentation.feature.city

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun CityScreen(
    state: CityState,
    onAction: (CityAction) -> Unit
) {
    // TODO UI Rendering
}

@Suppress("unused")
@Composable
@Preview(name = "City")
private fun CityScreenPreview(
    @PreviewParameter(CityStatePreviewParameterProvider::class)
    state: CityState
) {
    CityScreen(
        state = state,
        onAction = {}
    )
}

/**
 * PreviewParameter Provider for CityScreen Preview
 * Add values to the sequence to see the preview in different states
 **/
class CityStatePreviewParameterProvider : PreviewParameterProvider<CityState> {
    override val values: Sequence<CityState>
        get() = sequenceOf(
            CityState(),
        )
}
