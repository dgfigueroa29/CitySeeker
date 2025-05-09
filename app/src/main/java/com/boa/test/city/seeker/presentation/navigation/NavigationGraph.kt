package com.boa.test.city.seeker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.feature.main.MainScreen

@Suppress("FunctionNaming")
@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.MAIN.endpoint,
    ) {
        composable(Screen.MAIN.endpoint) {
            MainScreen()
        }
        composable(Screen.LIST.endpoint) {
            ListScreen()
        }
        composable(Screen.MAP.endpoint) {
            DetailScreen()
        }
    }
}
