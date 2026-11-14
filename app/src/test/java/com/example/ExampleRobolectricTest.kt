package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RecurrenceRule
import com.example.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Strukt", appName)
    }

    @Test
    fun `recurring task projects per-day completion and session logs non-destructively`() {
        val recurringTask = TaskEntity(
            id = 42L,
            title = "Daily Study Sprint",
            dateEpochDay = 20000L,
            recurrence = RecurrenceRule.DAILY.name,
            completedEpochDaysCsv = "20001",
            sessionSegmentsByDayCsv = "20001:540-570||580-610"
        )

        val dayOrigin = recurringTask.forEpochDay(20000L)
        assertFalse(dayOrigin.isCompleted)
        assertTrue(dayOrigin.parsedSessionSegments.isEmpty())

        val dayCompleted = recurringTask.forEpochDay(20001L)
        assertTrue(dayCompleted.isCompleted)
        assertEquals(2, dayCompleted.parsedSessionSegments.size)
        assertTrue(dayCompleted.hasBreakLoop)
        assertEquals(60, dayCompleted.totalWorkedMinutes)
        assertEquals(10, dayCompleted.totalBreakMinutes)
    }
}

