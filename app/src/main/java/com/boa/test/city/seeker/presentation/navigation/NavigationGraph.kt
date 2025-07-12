package com.boa.test.city.seeker.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.feature.main.MainScreen

/**
 * Composable function that defines the navigation graph for the application.
 *
 * It sets up the different screens (MainScreen, ListScreen, DetailScreen) and the transitions
 * between them.
 *
 * @param modifier Optional [Modifier] to be applied to the NavHost.
 * @param navController The [NavHostController] that manages app navigation.
 */
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
        composable(
            route = Screen.MAIN.endpoint,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }) {
            MainScreen(navController)
        }
        composable(
            route = Screen.LIST.endpoint,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }) {
            ListScreen(onCityClick = {
                navController.navigate("${Screen.MAP.endpoint}/${it}")
            })
        }
        composable(
            route = "${Screen.MAP.endpoint}/{cityId}",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId") ?: return@composable
            DetailScreen(navController = navController, cityId = cityId)
        }
    }
}
