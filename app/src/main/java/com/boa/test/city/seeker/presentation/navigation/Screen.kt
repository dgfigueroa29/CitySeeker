package com.boa.test.city.seeker.presentation.navigation

/**
 * Represents the different screens or destinations within the application.
 *
 * Each screen is associated with a unique endpoint string that can be used
 * for navigation, routing, or identifying the current screen in analytics.
 *
 * @property endpoint The unique string identifier for this screen.
 */
enum class Screen(val endpoint: String) {
    LIST("list"),
    MAP("map"),
    MAIN("main"),
}
