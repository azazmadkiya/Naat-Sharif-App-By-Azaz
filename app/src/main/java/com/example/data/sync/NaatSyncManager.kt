package com.example.data.sync

import android.content.Context
import com.example.data.repository.NaatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val updatedCount: Int, val timestamp: Long) : SyncState()
    data class Error(val message: String, val timestamp: Long) : SyncState()
}

/**
 * Manager handling background and on-demand synchronization between Firebase and Room.
 */
class NaatSyncManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val repository = NaatRepository.create(context)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    fun startBackgroundSync(intervalMillis: Long = NaatBackgroundSyncService.DEFAULT_SYNC_INTERVAL_MS) {
        NaatBackgroundSyncService.start(context)
        NaatBackgroundSyncService.schedulePeriodicAlarm(context, intervalMillis)
    }

    fun stopBackgroundSync() {
        NaatBackgroundSyncService.stop(context)
    }

    fun syncNow(onComplete: ((Result<Int>) -> Unit)? = null) {
        if (_syncState.value is SyncState.Syncing) return

        _syncState.value = SyncState.Syncing
        scope.launch {
            val result = repository.syncRemoteNaatsToLocalDb()
            result.onSuccess { count ->
                val now = System.currentTimeMillis()
                _syncState.value = SyncState.Success(count, now)
                onComplete?.invoke(Result.success(count))
            }.onFailure { error ->
                val now = System.currentTimeMillis()
                val msg = error.localizedMessage ?: "Sync failed"
                _syncState.value = SyncState.Error(msg, now)
                onComplete?.invoke(Result.failure(error))
            }
        }
    }
}
