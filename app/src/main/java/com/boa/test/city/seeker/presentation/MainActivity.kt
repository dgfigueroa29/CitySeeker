package com.boa.test.city.seeker.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.boa.test.city.seeker.data.source.PreferenceDataSource
import com.boa.test.city.seeker.presentation.navigation.NavigationGraph
import com.boa.test.city.seeker.presentation.navigation.Screen
import com.boa.test.city.seeker.presentation.ui.theme.CitySeekerTheme
import com.boa.test.city.seeker.presentation.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    @Inject
    lateinit var preferenceDataSource: PreferenceDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }
        lifecycleScope.launch {
            delay(500L)
            preferenceDataSource.isOnboardingCompleted()
            isReady = true
        }
        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.System) }
            CitySeekerTheme(themeMode = themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    navController = rememberNavController()
                    NavigationGraph(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        onThemeModeChanged = { themeMode = it },
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
