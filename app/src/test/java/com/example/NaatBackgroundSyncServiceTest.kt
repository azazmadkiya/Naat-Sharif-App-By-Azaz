package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.sync.NaatBackgroundSyncService
import com.example.data.sync.NaatSyncManager
import com.example.data.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaatBackgroundSyncServiceTest {

    @Test
    fun testServiceConstantsAndActions() {
        assertEquals("com.example.action.PERIODIC_SYNC", NaatBackgroundSyncService.ACTION_PERIODIC_SYNC)
        assertEquals("com.example.action.SYNC_NOW", NaatBackgroundSyncService.ACTION_SYNC_NOW)
        assertEquals("com.example.action.STOP_SYNC", NaatBackgroundSyncService.ACTION_STOP_SYNC)
        assertEquals("naat_sync_preferences", NaatBackgroundSyncService.PREFS_NAME)
        assertEquals(15 * 60 * 1000L, NaatBackgroundSyncService.DEFAULT_SYNC_INTERVAL_MS)
    }

    @Test
    fun testServiceSchedulingMethods() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Verify scheduling calls do not throw exceptions
        NaatBackgroundSyncService.start(context)
        NaatBackgroundSyncService.syncNow(context)
        NaatBackgroundSyncService.schedulePeriodicAlarm(context, 10 * 60 * 1000L)
        NaatBackgroundSyncService.stop(context)
    }

    @Test
    fun testSyncManagerInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = NaatSyncManager(context)
        assertNotNull(manager)
        assertTrue(manager.syncState.value is SyncState.Idle)
    }
}
