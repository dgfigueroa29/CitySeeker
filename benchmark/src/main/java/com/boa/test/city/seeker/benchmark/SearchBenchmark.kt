package com.boa.test.city.seeker.benchmark

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
class SearchBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun searchPerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.boa.test.city.seeker",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Full(),
            startupMode = StartupMode.WARM,
            iterations = 10,
        ) {
            pressHome()
            startActivityAndWait()

            // Wait for the search bar to appear
            device.wait(Until.hasObject(By.res("com.boa.test.city.seeker:id/searchBar")), 5_000)

            // Find and interact with search bar
            val searchField = device.findObject(By.res("com.boa.test.city.seeker:id/searchBar"))
            searchField?.let {
                it.click()
                device.waitForIdle()

                // Type search query
                it.text = "Denver"
                device.waitForIdle()

                // Clear and type another query
                it.text = ""
                device.waitForIdle()
                it.text = "Sydney"
                device.waitForIdle()
            }
        }
    }
}
