package com.boa.test.city.seeker.presentation.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.navigation.Screen

@Composable
fun MainScreen(navController: NavHostController? = null) {
    val landscape = isLandscape()

    if (landscape) {
        LandscapeLayout(navController)
    } else {
        PortraitLayout(navController)
    }
}

@Composable
fun PortraitLayout(navController: NavHostController? = null) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreen(onCityClick = {
            navController?.navigate("${Screen.MAP.endpoint}/${it}")
        })
    }
}

@Composable
fun LandscapeLayout(navController: NavHostController? = null) {
    var cityId by remember { mutableStateOf("0") }
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            ListScreen(onCityClick = {
                cityId = "$it"
            })
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            DetailScreen(navController, cityId = cityId)
        }
    }
}
