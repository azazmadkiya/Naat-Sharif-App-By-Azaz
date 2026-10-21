package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteNaatDao
import com.example.data.local.FavoriteNaatEntity
import com.example.data.local.toFavoriteEntity
import com.example.data.local.toNaatEntity
import com.example.data.local.NaatDao
import com.example.data.local.NaatEntity
import com.example.data.local.NaatLyricsDao
import com.example.data.local.NaatLyricsEntity
import com.example.data.local.OfflineNaatDao
import com.example.data.local.OfflineNaatEntity
import com.example.data.local.toNaatLyricsEntity
import com.example.data.local.toOfflineNaatEntity
import com.example.data.model.FirestoreAppStats
import com.example.data.model.RoleManager
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class NaatRepository(
    private val dao: NaatDao,
    private val favoriteDao: FavoriteNaatDao,
    private val firestore: FirebaseFirestore? = null,
    val lyricsDao: NaatLyricsDao? = null,
    val offlineDao: OfflineNaatDao? = null
) {
    private var naatsListener: ListenerRegistration? = null
    private var pendingListener: ListenerRegistration? = null

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val count = dao.countApproved()
            if (count == 0) {
                dao.insertAll(SeedData.INITIAL_NAATS)
                syncLocalSeedToFirestore()
            }
            // Seed default favorites in Room favorites table if empty
            if (favoriteDao.countFavorites() == 0) {
                val existingApproved = dao.getAllApprovedList()
                val seedFavorites = existingApproved.filter { it.isFavorite }
                if (seedFavorites.isNotEmpty()) {
                    favoriteDao.insertAllFavorites(seedFavorites.map { it.toFavoriteEntity() })
                }
            }
            // Populate offline lyrics and offline naats cache
            if (lyricsDao != null && lyricsDao.count() == 0) {
                val approved = dao.getAllApprovedList()
                if (approved.isNotEmpty()) {
                    lyricsDao.insertAll(approved.map { it.toNaatLyricsEntity() })
                }
            }
            if (offlineDao != null && offlineDao.count() == 0) {
                val approved = dao.getAllApprovedList()
                if (approved.isNotEmpty()) {
                    offlineDao.insertAll(approved.map { it.toOfflineNaatEntity() })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncLocalSeedToFirestore() {
        val auth = FirebaseAuth.getInstance()
        if (firestore != null && auth.currentUser != null) {
            try {
                for (naat in SeedData.INITIAL_NAATS) {
                    val doc = mapOf(
                        "id" to naat.id,
                        "titleHindi" to naat.titleHindi,
                        "titleGujarati" to naat.titleGujarati,
                        "titleEnglish" to naat.titleEnglish,
                        "categoryId" to naat.categoryId,
                        "categoryName" to naat.categoryName,
                        "poet" to naat.poet,
                        "publisher" to naat.publisher,
                        "lyricsHindi" to naat.lyricsHindi,
                        "lyricsGujarati" to naat.lyricsGujarati,
                        "lyricsEnglish" to naat.lyricsEnglish,
                        "submittedBy" to naat.submittedBy,
                        "addedByName" to naat.addedByName,
                        "isApproved" to naat.isApproved,
                        "isDeleted" to naat.isDeleted,
                        "orderIndex" to naat.orderIndex,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    firestore.collection("naats").document(naat.id).set(doc, SetOptions.merge())
                }
            } catch (e: Exception) {
                Log.e("NaatRepository", "Firestore seeding error: ${e.message}")
            }
        }
    }

    fun startFirestoreSync(scope: CoroutineScope) {
        if (firestore == null) return
        naatsListener?.remove()
        try {
            naatsListener = firestore.collection("naats")
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("isApproved", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("NaatRepository", "Firestore sync error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val remoteList = mutableListOf<NaatEntity>()
                            for (doc in snapshot.documents) {
                                val id = doc.getString("id") ?: doc.id
                                val titleHindi = doc.getString("titleHindi") ?: ""
                                val titleGujarati = doc.getString("titleGujarati") ?: ""
                                val titleEnglish = doc.getString("titleEnglish") ?: ""
                                val categoryId = doc.getString("categoryId") ?: "naat"
                                val categoryName = doc.getString("categoryName") ?: "नात शरीफ"
                                val poet = doc.getString("poet") ?: "Unknown"
                                val publisher = doc.getString("publisher") ?: ""
                                val lyricsHindi = doc.getString("lyricsHindi") ?: ""
                                val lyricsGujarati = doc.getString("lyricsGujarati") ?: ""
                                val lyricsEnglish = doc.getString("lyricsEnglish") ?: ""
                                val audioUrl = doc.getString("audioUrl")
                                val submittedBy = doc.getString("submittedBy") ?: "Azazmadkiya"
                                val addedByName = doc.getString("addedByName") ?: ""
                                val isApproved = doc.getBoolean("isApproved") ?: true
                                val isDeleted = doc.getBoolean("isDeleted") ?: false
                                val orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0

                                remoteList.add(
                                    NaatEntity(
                                        id = id,
                                        titleHindi = titleHindi,
                                        titleGujarati = titleGujarati,
                                        titleEnglish = titleEnglish,
                                        categoryId = categoryId,
                                        categoryName = categoryName,
                                        poet = poet,
                                        publisher = publisher,
                                        lyricsHindi = lyricsHindi,
                                        lyricsGujarati = lyricsGujarati,
                                        lyricsEnglish = lyricsEnglish,
                                        audioUrl = audioUrl,
                                        submittedBy = submittedBy,
                                        addedByName = addedByName,
                                        isApproved = isApproved,
                                        isDeleted = isDeleted,
                                        orderIndex = orderIndex
                                    )
                                )
                            }
                            if (remoteList.isNotEmpty()) {
                                dao.insertAll(remoteList)
                                lyricsDao?.insertAll(remoteList.map { it.toNaatLyricsEntity() })
                                offlineDao?.insertAll(remoteList.map { it.toOfflineNaatEntity() })
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("NaatRepository", "Failed to start Firestore sync", e)
        }
    }

    /**
     * Checks Firebase Firestore for approved Naat content and updates the local Room database,
     * ensuring all lyrics, authors, and language tags are stored locally for offline access.
     */
    suspend fun syncRemoteNaatsToLocalDb(): Result<Int> = withContext(Dispatchers.IO) {
        if (firestore == null) {
            return@withContext Result.failure(IllegalStateException("Firestore is not initialized"))
        }
        try {
            val snapshot = firestore.collection("naats")
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("isApproved", true)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return@withContext Result.success(0)
            }

            val remoteList = mutableListOf<NaatEntity>()
            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val titleHindi = doc.getString("titleHindi") ?: ""
                val titleGujarati = doc.getString("titleGujarati") ?: ""
                val titleEnglish = doc.getString("titleEnglish") ?: ""
                val categoryId = doc.getString("categoryId") ?: "naat"
                val categoryName = doc.getString("categoryName") ?: "नात शरीफ"
                val poet = doc.getString("poet") ?: "Unknown"
                val publisher = doc.getString("publisher") ?: ""
                val lyricsHindi = doc.getString("lyricsHindi") ?: ""
                val lyricsGujarati = doc.getString("lyricsGujarati") ?: ""
                val lyricsEnglish = doc.getString("lyricsEnglish") ?: ""
                val audioUrl = doc.getString("audioUrl")
                val submittedBy = doc.getString("submittedBy") ?: "Azazmadkiya"
                val addedByName = doc.getString("addedByName") ?: ""
                val isApproved = doc.getBoolean("isApproved") ?: true
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                val orderIndex = doc.getLong("orderIndex")?.toInt() ?: 0

                remoteList.add(
                    NaatEntity(
                        id = id,
                        titleHindi = titleHindi,
                        titleGujarati = titleGujarati,
                        titleEnglish = titleEnglish,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        poet = poet,
                        publisher = publisher,
                        lyricsHindi = lyricsHindi,
                        lyricsGujarati = lyricsGujarati,
                        lyricsEnglish = lyricsEnglish,
                        audioUrl = audioUrl,
                        submittedBy = submittedBy,
                        addedByName = addedByName,
                        isApproved = isApproved,
                        isDeleted = isDeleted,
                        orderIndex = orderIndex
                    )
                )
            }

            if (remoteList.isNotEmpty()) {
                dao.insertAll(remoteList)
                lyricsDao?.insertAll(remoteList.map { it.toNaatLyricsEntity() })
                offlineDao?.insertAll(remoteList.map { it.toOfflineNaatEntity() })
            }

            Result.success(remoteList.size)
        } catch (e: Exception) {
            Log.e("NaatRepository", "syncRemoteNaatsToLocalDb error", e)
            Result.failure(e)
        }
    }

    fun startAdminPendingSync(scope: CoroutineScope) {
        if (firestore == null) return
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) return
        pendingListener?.remove()
        try {
            pendingListener = firestore.collection("naats")
                .whereEqualTo("isDeleted", false)
                .whereEqualTo("isApproved", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("NaatRepository", "Firestore pending sync not available: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch(Dispatchers.IO) {
                            val list = mutableListOf<NaatEntity>()
                            for (doc in snapshot.documents) {
                                val id = doc.getString("id") ?: doc.id
                                val titleHindi = doc.getString("titleHindi") ?: ""
                                val titleGujarati = doc.getString("titleGujarati") ?: ""
                                val titleEnglish = doc.getString("titleEnglish") ?: ""
                                val categoryId = doc.getString("categoryId") ?: "naat"
                                val categoryName = doc.getString("categoryName") ?: "नात शरीफ"
                                val poet = doc.getString("poet") ?: "Unknown"
                                val publisher = doc.getString("publisher") ?: ""
                                val lyricsHindi = doc.getString("lyricsHindi") ?: ""
                                val lyricsGujarati = doc.getString("lyricsGujarati") ?: ""
                                val lyricsEnglish = doc.getString("lyricsEnglish") ?: ""
                                val submittedBy = doc.getString("submittedBy") ?: "Contributor"
                                val addedByName = doc.getString("addedByName") ?: ""
                                val isApproved = doc.getBoolean("isApproved") ?: false
                                val isDeleted = doc.getBoolean("isDeleted") ?: false

                                list.add(
                                    NaatEntity(
                                        id = id,
                                        titleHindi = titleHindi,
                                        titleGujarati = titleGujarati,
                                        titleEnglish = titleEnglish,
                                        categoryId = categoryId,
                                        categoryName = categoryName,
                                        poet = poet,
                                        publisher = publisher,
                                        lyricsHindi = lyricsHindi,
                                        lyricsGujarati = lyricsGujarati,
                                        lyricsEnglish = lyricsEnglish,
                                        submittedBy = submittedBy,
                                        addedByName = addedByName,
                                        isApproved = isApproved,
                                        isDeleted = isDeleted
                                    )
                                )
                            }
                            if (list.isNotEmpty()) {
                                dao.insertAll(list)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.d("NaatRepository", "Failed to start admin pending sync: ${e.message}")
        }
    }

    fun stopAdminPendingSync() {
        pendingListener?.remove()
        pendingListener = null
    }

    fun getFallbackUsers(): List<UserProfile> = listOf(
        UserProfile(
            uid = "superadmin_1",
            email = "azazmadkiya@gmail.com",
            fullName = "Azaz Madkiya",
            role = UserRole.SUPER_ADMIN,
            isGuest = false
        ),
        UserProfile(
            uid = "contributor_1",
            email = "info.azazmadkiya@gmail.com",
            fullName = "Azaz Madkiya (Contributor)",
            role = UserRole.CONTRIBUTOR,
            isGuest = false
        ),
        UserProfile(
            uid = "contributor_2",
            email = "madkiyaahemad@gmail.com",
            fullName = "Ahemadmadkiya",
            role = UserRole.CONTRIBUTOR,
            isGuest = false
        )
    )

    fun getUsersFromFirestore(): Flow<List<UserProfile>> = callbackFlow {
        if (firestore == null) {
            trySend(getFallbackUsers())
            awaitClose { }
            return@callbackFlow
        }
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(getFallbackUsers())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = mutableListOf<UserProfile>()
                    for (doc in snapshot.documents) {
                        val uid = doc.getString("uid") ?: doc.id
                        val email = doc.getString("email") ?: ""
                        val displayName = doc.getString("displayName") ?: email.substringBefore("@")
                        val roleStr = doc.getString("role") ?: "VIEWER"
                        val role = try {
                            UserRole.valueOf(roleStr)
                        } catch (e: Exception) {
                            RoleManager.getRoleForEmail(email)
                        }
                        list.add(
                            UserProfile(
                                uid = uid,
                                email = email,
                                fullName = displayName,
                                role = role,
                                isGuest = false
                            )
                        )
                    }
                    if (list.isEmpty()) {
                        trySend(getFallbackUsers())
                    } else {
                        trySend(list)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun listenToUserRole(uid: String, email: String = ""): Flow<UserRole> = callbackFlow {
        if (firestore == null || (uid.isBlank() && email.isBlank())) {
            trySend(RoleManager.getRoleForEmail(email))
            awaitClose { }
            return@callbackFlow
        }

        val targetDocId = if (uid.isNotBlank()) uid else email.trim().lowercase().replace(".", "_").replace("@", "_")
        val docRef = firestore.collection("users").document(targetDocId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("NaatRepository", "Firestore user role error: ${error.message}")
                trySend(RoleManager.getRoleForEmail(email))
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val roleStr = snapshot.getString("role")
                    ?: snapshot.getString("userRole")
                    ?: snapshot.getString("Role")
                    ?: ""
                if (roleStr.isNotBlank()) {
                    val parsedRole = UserRole.fromString(roleStr)
                    trySend(parsedRole)
                    return@addSnapshotListener
                }
            }

            trySend(RoleManager.getRoleForEmail(email))
        }

        awaitClose { listener.remove() }
    }

    suspend fun getUserRole(uid: String): UserRole = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext UserRole.READER
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            val roleStr = doc.getString("role") ?: doc.getString("userRole") ?: ""
            UserRole.fromString(roleStr)
        } catch (e: Exception) {
            UserRole.READER
        }
    }

    suspend fun updateUserRoleInFirestore(uid: String, email: String, newRole: UserRole) = withContext(Dispatchers.IO) {
        if (firestore != null && uid.isNotBlank()) {
            try {
                val data = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "role" to newRole.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("NaatRepository", "Failed to update role in Firestore: ${e.message}")
            }
        }
    }

    suspend fun assignUserRoleByEmail(email: String, name: String, role: UserRole) = withContext(Dispatchers.IO) {
        if (firestore != null && email.isNotBlank()) {
            try {
                val sanitizedId = email.trim().lowercase().replace(".", "_").replace("@", "_")
                val data = mapOf(
                    "uid" to sanitizedId,
                    "email" to email.trim().lowercase(),
                    "displayName" to if (name.isNotBlank()) name else email.substringBefore("@"),
                    "role" to role.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(sanitizedId).set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("NaatRepository", "Failed to assign role by email: ${e.message}")
            }
        }
    }

    suspend fun saveUserProfileToFirestore(profile: UserProfile) = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: profile.uid
        if (firestore != null && uid.isNotBlank()) {
            try {
                val data = mapOf(
                    "uid" to uid,
                    "email" to profile.email,
                    "displayName" to profile.fullName,
                    "role" to profile.role.name,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("NaatRepository", "Error saving user profile: ${e.message}")
            }
        }
    }

    fun getAllActiveNaats(): Flow<List<NaatEntity>> = dao.getAllApproved()

    fun getNaatsByCategory(categoryId: String): Flow<List<NaatEntity>> = dao.getByCategory(categoryId)

    fun getFavorites(): Flow<List<NaatEntity>> = favoriteDao.getAllFavorites().map { list ->
        list.map { it.toNaatEntity() }
    }

    fun getPendingApprovals(): Flow<List<NaatEntity>> = dao.getPendingApprovals()

    fun getTrash(): Flow<List<NaatEntity>> = dao.getTrash()

    fun getBySubmitter(email: String): Flow<List<NaatEntity>> = dao.getBySubmitter(email)

    fun search(query: String): Flow<List<NaatEntity>> = dao.searchNaats(query)

    fun getById(id: String): Flow<NaatEntity?> = dao.getById(id)

    fun isFavorite(naatId: String): Flow<Boolean> = favoriteDao.isFavorite(naatId)

    suspend fun isFavoriteSync(naatId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.isFavoriteSync(naatId)
    }

    suspend fun addToFavorites(naat: NaatEntity) = withContext(Dispatchers.IO) {
        try {
            favoriteDao.insertFavorite(naat.toFavoriteEntity())
            dao.setFavorite(naat.id, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeFromFavorites(naatId: String) = withContext(Dispatchers.IO) {
        try {
            favoriteDao.deleteFavoriteById(naatId)
            dao.setFavorite(naatId, false)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun toggleFavorite(naat: NaatEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val isFav = favoriteDao.isFavoriteSync(naat.id)
            if (isFav) {
                favoriteDao.deleteFavoriteById(naat.id)
                dao.setFavorite(naat.id, false)
                false
            } else {
                favoriteDao.insertFavorite(naat.toFavoriteEntity())
                dao.setFavorite(naat.id, true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleFavorite(id: String, currentFav: Boolean) = withContext(Dispatchers.IO) {
        try {
            if (currentFav) {
                favoriteDao.deleteFavoriteById(id)
                dao.setFavorite(id, false)
            } else {
                val naat = dao.getAllApprovedList().find { it.id == id }
                if (naat != null) {
                    favoriteDao.insertFavorite(naat.toFavoriteEntity())
                }
                dao.setFavorite(id, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertOrUpdateNaat(naat: NaatEntity) = withContext(Dispatchers.IO) {
        try {
            dao.insert(naat)
            lyricsDao?.insert(naat.toNaatLyricsEntity())
            offlineDao?.insert(naat.toOfflineNaatEntity())
            val auth = FirebaseAuth.getInstance()
            if (firestore != null && auth.currentUser != null) {
                val doc = mapOf(
                    "id" to naat.id,
                    "titleHindi" to naat.titleHindi,
                    "titleGujarati" to naat.titleGujarati,
                    "titleEnglish" to naat.titleEnglish,
                    "categoryId" to naat.categoryId,
                    "categoryName" to naat.categoryName,
                    "poet" to naat.poet,
                    "publisher" to naat.publisher,
                    "lyricsHindi" to naat.lyricsHindi,
                    "lyricsGujarati" to naat.lyricsGujarati,
                    "lyricsEnglish" to naat.lyricsEnglish,
                    "submittedBy" to naat.submittedBy,
                    "addedByName" to naat.addedByName,
                    "isApproved" to naat.isApproved,
                    "isDeleted" to naat.isDeleted,
                    "orderIndex" to naat.orderIndex,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("naats").document(naat.id).set(doc, SetOptions.merge())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun approveNaat(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.approve(id)
            val auth = FirebaseAuth.getInstance()
            if (firestore != null && auth.currentUser != null) {
                firestore.collection("naats").document(id)
                    .update("isApproved", true, "updatedAt", FieldValue.serverTimestamp())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun softDelete(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.softDelete(id)
            val auth = FirebaseAuth.getInstance()
            if (firestore != null && auth.currentUser != null) {
                firestore.collection("naats").document(id)
                    .update("isDeleted", true, "updatedAt", FieldValue.serverTimestamp())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun restore(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.restore(id)
            val auth = FirebaseAuth.getInstance()
            if (firestore != null && auth.currentUser != null) {
                firestore.collection("naats").document(id)
                    .update("isDeleted", false, "updatedAt", FieldValue.serverTimestamp())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun permanentDelete(id: String) = withContext(Dispatchers.IO) {
        try {
            dao.deletePermanently(id)
            val auth = FirebaseAuth.getInstance()
            if (firestore != null && auth.currentUser != null) {
                firestore.collection("naats").document(id).delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getStats(): Map<String, Int> = withContext(Dispatchers.IO) {
        try {
            mapOf(
                "totalApproved" to dao.countApproved(),
                "pendingApprovals" to dao.countPending(),
                "trashCount" to dao.countTrash()
            )
        } catch (e: Exception) {
            mapOf("totalApproved" to 0, "pendingApprovals" to 0, "trashCount" to 0)
        }
    }

    suspend fun fetchFirestoreApplicationStats(): FirestoreAppStats = withContext(Dispatchers.IO) {
        val auth = FirebaseAuth.getInstance()
        if (firestore != null && auth.currentUser != null) {
            try {
                val approvedSnap = firestore.collection("naats")
                    .whereEqualTo("isDeleted", false)
                    .whereEqualTo("isApproved", true)
                    .get()
                    .await()

                val catCounts = mutableMapOf<String, Int>()
                for (doc in approvedSnap.documents) {
                    val catName = doc.getString("categoryName") ?: "हम्द / नात"
                    catCounts[catName] = (catCounts[catName] ?: 0) + 1
                }

                var pendingCount = dao.countPending()
                var trashCount = dao.countTrash()
                var usersCount = 3
                var contribCount = 2

                try {
                    val pendingSnap = firestore.collection("naats")
                        .whereEqualTo("isDeleted", false)
                        .whereEqualTo("isApproved", false)
                        .get()
                        .await()
                    pendingCount = pendingSnap.size()

                    val trashSnap = firestore.collection("naats")
                        .whereEqualTo("isDeleted", true)
                        .get()
                        .await()
                    trashCount = trashSnap.size()

                    val usersSnap = firestore.collection("users").get().await()
                    usersCount = usersSnap.size().coerceAtLeast(3)

                    var cCount = 0
                    for (doc in usersSnap.documents) {
                        val role = doc.getString("role") ?: ""
                        if (role == "CONTRIBUTOR" || role == "ADMIN" || role == "SUPER_ADMIN") {
                            cCount++
                        }
                    }
                    contribCount = cCount.coerceAtLeast(2)
                } catch (e: Exception) {
                    Log.d("NaatRepository", "Admin stats restricted for user, fallback to local: ${e.message}")
                }

                FirestoreAppStats(
                    totalApproved = approvedSnap.size(),
                    pendingReview = pendingCount,
                    trashedCount = trashCount,
                    totalUsers = usersCount,
                    contributorsCount = contribCount,
                    categoryCounts = if (catCounts.isEmpty()) getLocalCategoryCounts() else catCounts,
                    isLiveFromFirestore = true,
                    lastSyncTime = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.d("NaatRepository", "Firestore stats query skipped: ${e.message}")
                getLocalAppStats()
            }
        } else {
            getLocalAppStats()
        }
    }

    private suspend fun getLocalCategoryCounts(): Map<String, Int> {
        val allList = dao.getAllApprovedList()
        val catCounts = mutableMapOf<String, Int>()
        for (item in allList) {
            catCounts[item.categoryName] = (catCounts[item.categoryName] ?: 0) + 1
        }
        return catCounts
    }

    private suspend fun getLocalAppStats(): FirestoreAppStats {
        val approved = dao.countApproved()
        val pending = dao.countPending()
        val trash = dao.countTrash()
        return FirestoreAppStats(
            totalApproved = approved,
            pendingReview = pending,
            trashedCount = trash,
            totalUsers = 3,
            contributorsCount = 2,
            categoryCounts = getLocalCategoryCounts(),
            isLiveFromFirestore = false,
            lastSyncTime = System.currentTimeMillis()
        )
    }

    suspend fun exportToJson(): String = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray()
            val naats = dao.getAllApprovedList()
            for (naat in naats) {
                val obj = JSONObject().apply {
                    put("id", naat.id)
                    put("titleHindi", naat.titleHindi)
                    put("titleGujarati", naat.titleGujarati)
                    put("titleEnglish", naat.titleEnglish)
                    put("categoryId", naat.categoryId)
                    put("categoryName", naat.categoryName)
                    put("poet", naat.poet)
                    put("lyricsHindi", naat.lyricsHindi)
                    put("lyricsGujarati", naat.lyricsGujarati)
                    put("lyricsEnglish", naat.lyricsEnglish)
                    put("audioUrl", naat.audioUrl ?: "")
                    put("orderIndex", naat.orderIndex)
                }
                jsonArray.put(obj)
            }
            jsonArray.toString(2)
        } catch (e: Exception) {
            "[]"
        }
    }

    suspend fun importFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<NaatEntity>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    NaatEntity(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        titleHindi = obj.optString("titleHindi", "Naat"),
                        titleGujarati = obj.optString("titleGujarati", ""),
                        titleEnglish = obj.optString("titleEnglish", ""),
                        categoryId = obj.optString("categoryId", "naat"),
                        categoryName = obj.optString("categoryName", "नात शरीफ"),
                        poet = obj.optString("poet", "Unknown"),
                        lyricsHindi = obj.optString("lyricsHindi", ""),
                        lyricsGujarati = obj.optString("lyricsGujarati", ""),
                        lyricsEnglish = obj.optString("lyricsEnglish", ""),
                        audioUrl = obj.optString("audioUrl", null),
                        isApproved = true,
                        isDeleted = false
                    )
                )
            }
            if (list.isNotEmpty()) {
                dao.insertAll(list)
            }
            list.size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    companion object {
        fun create(context: Context): NaatRepository {
            val db = AppDatabase.getInstance(context)
            val firestore = try {
                val dbId = context.getString(R.string.firestore_database_id)
                FirebaseFirestore.getInstance(dbId)
            } catch (e: Exception) {
                null
            }
            return NaatRepository(
                dao = db.naatDao(),
                favoriteDao = db.favoriteNaatDao(),
                firestore = firestore,
                lyricsDao = db.naatLyricsDao(),
                offlineDao = db.offlineNaatDao()
            )
        }
    }
}
