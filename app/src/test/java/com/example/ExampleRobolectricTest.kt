package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.AiRunnerService
import com.example.ui.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Psyco Time X Pro", appName)
    }

    @Test
    fun `verify stopwatch millisecond formatting`() {
        val formatted = MainViewModel.formatMillisToStopwatch(10582L)
        assertEquals("00:10.582", formatted)

        val zero = MainViewModel.formatMillisToStopwatch(0L)
        assertEquals("00:00.000", zero)

        val oneMin = MainViewModel.formatMillisToStopwatch(65045L)
        assertEquals("01:05.045", oneMin)
    }

    @Test
    fun `verify warm up plan structure generation`() {
        val service = AiRunnerService()
        val plan = service.getStructuredWarmUpPlan("100m Pecut", "Remaja / Sukma")
        assertEquals("100m Pecut", plan.targetEvent)
        assertTrue(plan.phases.isNotEmpty())
        assertTrue(plan.totalDurationMinutes >= 30)
    }
}
