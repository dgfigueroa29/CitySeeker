package com.boa.test.city.seeker.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.boa.test.city.seeker.presentation.feature.city.detail.DetailScreen
import com.boa.test.city.seeker.presentation.feature.city.list.ListScreen
import com.boa.test.city.seeker.presentation.feature.main.MainScreen
import com.boa.test.city.seeker.presentation.feature.onboarding.OnboardingScreen
import com.boa.test.city.seeker.presentation.feature.onboarding.OnboardingViewModel
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode

private const val NAV_DURATION = 400

@Suppress("FunctionNaming")
@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onThemeModeChanged: (ThemeMode) -> Unit = {},
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isLoading by onboardingViewModel.isLoading.collectAsState()
    val isOnboardingCompleted by onboardingViewModel.isCompleted.collectAsState()

    if (isLoading) return

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (isOnboardingCompleted) Screen.MAIN.endpoint else Screen.ONBOARDING.endpoint,
    ) {
        composable(
            route = Screen.ONBOARDING.endpoint,
            enterTransition = { fadeIn(animationSpec = tween(NAV_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(NAV_DURATION)) },
        ) {
            OnboardingScreen(
                onCompleted = {
                    onboardingViewModel.completeOnboarding()
                    navController.navigate(Screen.MAIN.endpoint) {
                        popUpTo(Screen.ONBOARDING.endpoint) { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingViewModel.skipOnboarding()
                    navController.navigate(Screen.MAIN.endpoint) {
                        popUpTo(Screen.ONBOARDING.endpoint) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Screen.MAIN.endpoint,
            enterTransition = { fadeIn(animationSpec = tween(NAV_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(NAV_DURATION)) },
        ) {
            MainScreen(navController)
        }
        composable(
            route = Screen.LIST.endpoint,
            enterTransition = { fadeIn(animationSpec = tween(NAV_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(NAV_DURATION)) },
        ) {
            ListScreen(
                onCityClick = {
                    navController.navigate("${Screen.MAP.endpoint}/$it")
                },
                onThemeModeChanged = onThemeModeChanged,
            )
        }
        composable(
            route = "${Screen.MAP.endpoint}/{cityId}",
            deepLinks =
                listOf(
                    navDeepLink { uriPattern = "cityseeker://city/{cityId}" },
                ),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(NAV_DURATION),
                ) + fadeIn(animationSpec = tween(NAV_DURATION))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(NAV_DURATION),
                ) + fadeOut(animationSpec = tween(NAV_DURATION))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(NAV_DURATION),
                ) + fadeIn(animationSpec = tween(NAV_DURATION))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(NAV_DURATION),
                ) + fadeOut(animationSpec = tween(NAV_DURATION))
            },
        ) { backStackEntry ->
            val cityId = backStackEntry.arguments?.getString("cityId") ?: return@composable
            DetailScreen(navController = navController, cityId = cityId)
        }
    }
}
