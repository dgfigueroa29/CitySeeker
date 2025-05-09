package com.boa.test.city.seeker.presentation.feature.city.detail


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
