package com.boa.test.city.seeker.presentation.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.navigation.Screen

/**
 * Composable function that displays the main screen of the application.
 *
 * It checks the device orientation and displays the appropriate layout (Landscape or Portrait).
 *
 * @param navController The NavHostController used for navigation.
 */
@Composable
fun MainScreen(navController: NavHostController? = null) {
    val landscape = isLandscape()

    if (landscape) {
        LandscapeLayout(navController)
    } else {
        PortraitLayout(navController)
    }
}

/**
 * Composable function for the portrait layout of the main screen.
 * It displays a list of cities and navigates to the map screen when a city is clicked.
 *
 * @param navController The NavHostController for navigation.
 */
@Composable
fun PortraitLayout(navController: NavHostController? = null) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreen(onCityClick = {
            navController?.navigate("${Screen.MAP.endpoint}/${it}")
        })
    }
}

/**
 * Composable function for displaying the main screen in landscape orientation.
 * It shows a list of cities on the left and the details of the selected city on the right.
 *
 * @param navController The navigation controller for handling navigation events.
 */
@Composable
fun LandscapeLayout(navController: NavHostController? = null) {
    var cityId by rememberSaveable { mutableStateOf("0") }
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            ListScreen(onCityClick = {
                cityId = it
            })
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            DetailScreen(navController = navController, cityId = cityId)
        }
    }
}
