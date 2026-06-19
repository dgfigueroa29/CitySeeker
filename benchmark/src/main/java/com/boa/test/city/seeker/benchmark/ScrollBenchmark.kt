package com.boa.test.city.seeker.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollList() {
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

            // Find the LazyColumn and scroll
            val list = device.findObject(By.res("com.boa.test.city.seeker:id/lazyColumn"))
            list?.let {
                it.setGestureMargin(device.displayWidth / 5)
                it.fling(Direction.DOWN)
                device.waitForIdle()
                it.fling(Direction.UP)
            }
        }
    }
}
