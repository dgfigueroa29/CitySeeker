package com.boa.test.city.seeker.presentation.component

import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FilterSwitchTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var currentShowingFavorites = false
    private var changedCount = 0

    @Test
    fun showsAllCitiesLabel() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = false,
                onShowFavoritesChanged = {},
            )
        }
        composeTestRule.onNodeWithText("All").assertExists()
    }

    @Test
    fun showsFavCitiesLabel() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = false,
                onShowFavoritesChanged = {},
            )
        }
        composeTestRule.onNodeWithText("Favorite").assertExists()
    }

    @Test
    fun showsBadgeWhenShowingFavoritesWithCount() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = true,
                favoriteCount = 3,
                onShowFavoritesChanged = {},
            )
        }
        composeTestRule.onNodeWithText("3").assertExists()
    }

    @Test
    fun badgeHiddenWhenNotShowingFavorites() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = false,
                favoriteCount = 3,
                onShowFavoritesChanged = {},
            )
        }
        composeTestRule.onNodeWithText("3").assertDoesNotExist()
    }

    @Test
    fun togglesOnClick() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = currentShowingFavorites,
                onShowFavoritesChanged = { newValue ->
                    currentShowingFavorites = newValue
                    changedCount++
                },
            )
        }

        val switch = composeTestRule.onNodeWithText("All")
        switch.performClick()

        assertTrue(currentShowingFavorites)
    }

    @Test
    fun switchIsToggleable() {
        composeTestRule.setContent {
            FilterSwitch(
                isShowingFavorites = false,
                onShowFavoritesChanged = {},
            )
        }
        composeTestRule.onNodeWithText("All").assertIsToggleable()
    }
}
