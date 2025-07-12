package com.boa.test.city.seeker.presentation.component

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Composable function to determine if the current device orientation is landscape.
 *
 * @return `true` if the device is in landscape orientation, `false` otherwise.
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
