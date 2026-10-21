package com.example.data.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import com.example.data.repository.NaatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Background sync service that periodically checks Firebase for new Naat content
 * and updates the local Room database to ensure users have the latest lyrics available offline.
 */
class NaatBackgroundSyncService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var periodicJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NaatBackgroundSyncService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PERIODIC_SYNC
        Log.d(TAG, "NaatBackgroundSyncService onStartCommand with action: $action")

        when (action) {
            ACTION_SYNC_NOW -> {
                performSingleSync()
            }
            ACTION_STOP_SYNC -> {
                stopSelf()
            }
            ACTION_PERIODIC_SYNC -> {
                startPeriodicSyncLoop()
            }
            else -> {
                startPeriodicSyncLoop()
            }
        }

        return START_STICKY
    }

    private fun startPeriodicSyncLoop() {
        if (periodicJob?.isActive == true) return
        periodicJob = serviceScope.launch {
            Log.d(TAG, "Starting periodic sync loop (interval: ${DEFAULT_SYNC_INTERVAL_MS}ms)")
            while (isActive) {
                try {
                    doSync()
                } catch (e: Exception) {
                    Log.e(TAG, "Periodic sync check error: ${e.message}", e)
                }
                delay(DEFAULT_SYNC_INTERVAL_MS)
            }
        }
    }

    private fun performSingleSync() {
        serviceScope.launch {
            try {
                doSync()
            } catch (e: Exception) {
                Log.e(TAG, "Single sync execution error: ${e.message}", e)
            }
        }
    }

    private suspend fun doSync() {
        Log.d(TAG, "Syncing: Checking Firebase Firestore for latest Naat content...")
        val repository = NaatRepository.create(applicationContext)
        val result = repository.syncRemoteNaatsToLocalDb()
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        result.onSuccess { count ->
            Log.d(TAG, "Sync succeeded: Updated $count Naats into local Room database for offline access.")
            prefs.edit()
                .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                .putInt(KEY_LAST_SYNC_COUNT, count)
                .putBoolean(KEY_LAST_SYNC_SUCCESS, true)
                .putString(KEY_LAST_SYNC_ERROR, "")
                .apply()
        }.onFailure { error ->
            Log.w(TAG, "Sync finished with warning: ${error.message}")
            prefs.edit()
                .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                .putBoolean(KEY_LAST_SYNC_SUCCESS, false)
                .putString(KEY_LAST_SYNC_ERROR, error.localizedMessage ?: "Unknown error")
                .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NaatBackgroundSyncService destroyed")
        periodicJob?.cancel()
        serviceScope.cancel()
    }

    companion object {
        const val TAG = "NaatBackgroundSync"
        const val PREFS_NAME = "naat_sync_preferences"
        const val KEY_LAST_SYNC_TIME = "key_last_sync_time"
        const val KEY_LAST_SYNC_COUNT = "key_last_sync_count"
        const val KEY_LAST_SYNC_SUCCESS = "key_last_sync_success"
        const val KEY_LAST_SYNC_ERROR = "key_last_sync_error"

        const val ACTION_PERIODIC_SYNC = "com.example.action.PERIODIC_SYNC"
        const val ACTION_SYNC_NOW = "com.example.action.SYNC_NOW"
        const val ACTION_STOP_SYNC = "com.example.action.STOP_SYNC"

        // Default 15 minutes periodic interval
        const val DEFAULT_SYNC_INTERVAL_MS = 15 * 60 * 1000L

        fun start(context: Context) {
            val intent = Intent(context, NaatBackgroundSyncService::class.java).apply {
                action = ACTION_PERIODIC_SYNC
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service: ${e.message}")
            }
        }

        fun syncNow(context: Context) {
            val intent = Intent(context, NaatBackgroundSyncService::class.java).apply {
                action = ACTION_SYNC_NOW
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send syncNow intent: ${e.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NaatBackgroundSyncService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
            try {
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop service: ${e.message}")
            }
        }

        /**
         * Schedules periodic wakeups using AlarmManager to ensure recurring background execution
         */
        fun schedulePeriodicAlarm(context: Context, intervalMillis: Long = DEFAULT_SYNC_INTERVAL_MS) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, NaatBackgroundSyncService::class.java).apply {
                action = ACTION_PERIODIC_SYNC
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getService(context, 1002, intent, flags)

            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
            Log.d(TAG, "AlarmManager scheduled periodic sync interval: ${intervalMillis / 1000}s")
        }
    }
}
