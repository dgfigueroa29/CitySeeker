package com.boa.test.city.seeker.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        baselineRule.collect(
            packageName = "com.boa.test.city.seeker",
        ) {
            // Cold start the app
            pressHome()
            startActivityAndWait()

            // Wait for the list to load
            device.waitForIdle()

            // Scroll through the list
            device.findObject(
                androidx.test.uiautomator.By.res("com.boa.test.city.seeker:id/lazyColumn"),
            )?.let { list ->
                list.setGestureMargin(device.displayWidth / 5)
                list.fling(androidx.test.uiautomator.Direction.DOWN)
                device.waitForIdle()
                list.fling(androidx.test.uiautomator.Direction.UP)
            }

            // Click on a city to open detail
            device.findObject(
                androidx.test.uiautomator.By.res("com.boa.test.city.seeker:id/cityItem"),
            )?.click()
            device.waitForIdle()

            // Go back
            device.pressBack()
            device.waitForIdle()
        }
    }
}
