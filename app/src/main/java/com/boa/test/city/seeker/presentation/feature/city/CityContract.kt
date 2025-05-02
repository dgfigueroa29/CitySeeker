package com.boa.test.city.seeker.presentation.feature.city

import kotlinx.serialization.Serializable


/**
 * Object used for a type safe destination to a City route
 */
@Serializable
object CityDestination

/**
 * UI State that represents CityScreen
 **/
class CityState

/**
 * City Actions emitted from the UI Layer
 * passed to the coordinator to handle
 **/

sealed interface CityAction {
    data object OnClick : CityAction
}

