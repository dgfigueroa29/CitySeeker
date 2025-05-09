package com.boa.test.city.seeker.presentation.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.boa.test.city.seeker.presentation.component.isLandscape
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen

@Composable
fun MainScreen() {
    val landscape = isLandscape()

    if (landscape) {
        LandscapeLayout()
    } else {
        PortraitLayout()
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        ListScreen()
    }
}

@Composable
fun LandscapeLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            ListScreen()
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            DetailScreen()
        }
    }
}
