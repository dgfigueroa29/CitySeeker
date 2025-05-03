package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.serialization.Serializable


/**
 * Object used for a type safe destination to a List route
 */
@Serializable
object ListDestination

/**
 * UI State that represents ListScreen
 **/
class ListState

/**
 * List Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/

sealed interface ListAction {
    data object OnClick : ListAction
}
