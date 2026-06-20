package com.boa.test.city.seeker.presentation.component

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
class SuccessSnackbarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsMessage() {
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("City added")
            }
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        SuccessSnackbar(snackbarData = data)
                    }
                },
            ) {}
        }
        composeTestRule.onNodeWithText("City added").assertExists()
    }

    @Test
    fun showsDifferentMessage() {
        val snackbarHostState = SnackbarHostState()
        composeTestRule.setContent {
            LaunchedEffect(Unit) {
                snackbarHostState.showSnackbar("City removed")
            }
            Scaffold(
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        SuccessSnackbar(snackbarData = data)
                    }
                },
            ) {}
        }
        composeTestRule.onNodeWithText("City removed").assertExists()
    }
}
