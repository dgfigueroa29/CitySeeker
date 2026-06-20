package com.boa.test.city.seeker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.boa.test.city.seeker.domain.repository.PreferenceRepository
import com.boa.test.city.seeker.presentation.feature.onboarding.OnboardingViewModel
import com.boa.test.city.seeker.presentation.navigation.NavigationGraph
import com.boa.test.city.seeker.presentation.navigation.Screen
import com.boa.test.city.seeker.presentation.ui.theme.CitySeekerTheme
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }
        enableEdgeToEdge()
        setContent {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val isLoading by onboardingViewModel.isLoading.collectAsState()
            isReady = !isLoading

            var themeMode by remember { mutableStateOf(ThemeMode.System) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val saved = preferenceRepository.getThemeMode()
                themeMode = try { ThemeMode.valueOf(saved) } catch (_: Exception) { ThemeMode.System }
            }

            CitySeekerTheme(themeMode = themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    navController = rememberNavController()
                    NavigationGraph(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        onThemeModeChanged = { newMode ->
                            themeMode = newMode
                            scope.launch { preferenceRepository.setThemeMode(newMode.name) }
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            if (uri.scheme == "cityseeker" && uri.host == "city") {
                uri.lastPathSegment?.let { cityId ->
                    if (::navController.isInitialized) {
                        navController.navigate("${Screen.MAP.endpoint}/$cityId")
                    }
                }
            }
        }
    }
}
