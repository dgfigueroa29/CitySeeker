package com.boa.test.city.seeker.presentation.feature.city.list

/**
 * Object used for a type safe destination to a List route
 */
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
