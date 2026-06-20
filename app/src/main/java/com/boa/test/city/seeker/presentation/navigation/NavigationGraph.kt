package com.boa.test.city.seeker.presentation.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.boa.test.city.seeker.presentation.feature.journal.JournalScreen
import com.boa.test.city.seeker.presentation.feature.onboarding.OnboardingScreen
import com.boa.test.city.seeker.presentation.feature.onboarding.OnboardingViewModel
import com.boa.test.city.seeker.presentation.feature.region.RegionSelectorScreen
import com.boa.test.city.seeker.presentation.ui.theme.LocalAnimatedVisibilityScope
import com.boa.test.city.seeker.presentation.ui.theme.LocalSharedTransitionScope
import com.boa.test.city.seeker.presentation.ui.theme.MotionDuration
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode
import com.boa.test.city.seeker.presentation.component.LoadingIndicator
import com.boa.test.city.seeker.presentation.feature.ar.ArCityScreen
import com.boa.test.city.seeker.presentation.feature.route.RouteScreen

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

    if (isLoading) {
        LoadingIndicator(isLoading = true)
        return
    }

    SharedTransitionLayout(modifier = modifier) {
        val sharedTransitionScope = this
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingCompleted) Screen.MAIN.endpoint else Screen.ONBOARDING.endpoint,
        ) {
            composable(
                route = Screen.ONBOARDING.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
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
            }
            composable(
                route = Screen.MAIN.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                    MainScreen(navController)
                }
            }
            composable(
                route = Screen.REGIONS.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                    RegionSelectorScreen(
                        onCityClick = { cityId ->
                            navController.navigate("${Screen.MAP.endpoint}/$cityId")
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(
                route = Screen.JOURNALS.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                    JournalScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onCityClick = { cityId ->
                            navController.navigate("${Screen.MAP.endpoint}/$cityId")
                        },
                    )
                }
            }
            composable(
                route = Screen.LIST.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides sharedTransitionScope,
                    LocalAnimatedVisibilityScope provides this,
                ) {
                    ListScreen(
                        onCityClick = {
                            navController.navigate("${Screen.MAP.endpoint}/$it")
                        },
                        onThemeModeChanged = onThemeModeChanged,
                    )
                }
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
                        animationSpec = tween(MotionDuration.ScreenTransition),
                    ) + fadeIn(animationSpec = tween(MotionDuration.ScreenTransition))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(MotionDuration.ScreenTransition),
                    ) + fadeOut(animationSpec = tween(MotionDuration.ScreenTransition))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(MotionDuration.ScreenTransition),
                    ) + fadeIn(animationSpec = tween(MotionDuration.ScreenTransition))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(MotionDuration.ScreenTransition),
                    ) + fadeOut(animationSpec = tween(MotionDuration.ScreenTransition))
                },
            ) { backStackEntry ->
                val cityId = backStackEntry.arguments?.getString("cityId") ?: return@composable
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides sharedTransitionScope,
                    LocalAnimatedVisibilityScope provides this,
                ) {
                    DetailScreen(navController = navController, cityId = cityId)
                }
            }
            composable(
                route = "${Screen.AR.endpoint}/{cityId}",
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) { backStackEntry ->
                val cityId = backStackEntry.arguments?.getString("cityId")?.toLongOrNull() ?: return@composable
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                    ArCityScreen(
                        cityId = cityId,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(
                route = Screen.ROUTE.endpoint,
                enterTransition = { fadeIn(animationSpec = tween(MotionDuration.ScreenTransition)) },
                exitTransition = { fadeOut(animationSpec = tween(MotionDuration.ScreenTransition)) },
            ) {
                CompositionLocalProvider(LocalSharedTransitionScope provides sharedTransitionScope) {
                    RouteScreen(
                        onCityClick = { cityId ->
                            navController.navigate("${Screen.MAP.endpoint}/$cityId")
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
