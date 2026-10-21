package com.example.data.model

/**
 * Live application statistics aggregated from Firebase Firestore.
 */
data class FirestoreAppStats(
    val totalApproved: Int = 0,
    val pendingReview: Int = 0,
    val trashedCount: Int = 0,
    val totalUsers: Int = 0,
    val contributorsCount: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap(),
    val isLiveFromFirestore: Boolean = false,
    val lastSyncTime: Long = System.currentTimeMillis()
)
