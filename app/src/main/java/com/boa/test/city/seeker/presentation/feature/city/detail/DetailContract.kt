package com.boa.test.city.seeker.presentation.feature.city.detail

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.serialization.Serializable


/**
 * Object used for a type safe destination to a Detail route
 */
@Serializable
object DetailDestination

/**
 * UI State that represents DetailScreen
 **/
class DetailState

/**
 * Detail Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/

sealed interface DetailAction {
    data object OnClick : DetailAction
}
