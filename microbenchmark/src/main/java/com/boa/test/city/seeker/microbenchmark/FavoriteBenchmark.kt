package com.boa.test.city.seeker.microbenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun toggleFavoritePerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.boa.test.city.seeker",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Full(),
            startupMode = StartupMode.WARM,
            iterations = 10,
        ) {
            pressHome()
            startActivityAndWait()

            // Wait for the list to load
            device.wait(Until.hasObject(By.res("com.boa.test.city.seeker:id/lazyColumn")), 5_000)

            // Find and click favorite button on first item
            val favoriteButton = device.findObject(By.res("com.boa.test.city.seeker:id/favoriteButton"))
            favoriteButton?.let {
                it.click()
                device.waitForIdle()
                it.click() // Toggle back
                device.waitForIdle()
            }
        }
    }
}
