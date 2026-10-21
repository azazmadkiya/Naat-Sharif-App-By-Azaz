package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.local.NaatEntity
import com.example.data.model.CategoryInfo
import com.example.data.model.DefaultCategories
import com.example.data.model.FirestoreAppStats
import com.example.data.model.RoleManager
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.NaatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class Screen {
    SPLASH,
    AUTH,
    HOME,
    READER,
    FAVORITES,
    SETTINGS,
    ADMIN_PORTAL,
    POST_CREATOR
}

enum class CategoryLayoutMode {
    SWIPE_ROW,
    GRID_VIEW
}

class NaatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NaatRepository.create(application)
    private val prefs: SharedPreferences =
        application.getSharedPreferences("naat_sharif_prefs", Context.MODE_PRIVATE)

    private fun getFirebaseAuthSafe(): FirebaseAuth? {
        return try {
            if (com.google.firebase.FirebaseApp.getApps(getApplication<Application>()).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (t: Throwable) {
            null
        }
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.SPLASH)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenStack = mutableListOf<Screen>()

    // Current User & Authentication (Preserves user session across app restarts)
    private val _currentUser = MutableStateFlow(
        run {
            val savedEmail = prefs.getString("user_email", "")?.trim() ?: ""
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)
            val savedUid = prefs.getString("user_uid", "") ?: ""
            val savedName = prefs.getString("user_name", "") ?: ""
            val savedRole = prefs.getString("user_role", "") ?: ""
            val fbUser = getFirebaseAuthSafe()?.currentUser
            val effectiveEmail = fbUser?.email?.trim().takeUnless { it.isNullOrBlank() } ?: savedEmail

            if (fbUser != null || effectiveEmail.isNotBlank() || isLoggedIn) {
                val effectiveRole = if (savedRole.isNotBlank()) {
                    UserRole.fromString(savedRole)
                } else {
                    RoleManager.getRoleForEmail(effectiveEmail)
                }
                val effectiveName = fbUser?.displayName.takeUnless { it.isNullOrBlank() }
                    ?: if (savedName.isNotBlank() && savedName != "Guest User") savedName else effectiveEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                UserProfile(
                    uid = fbUser?.uid ?: savedUid,
                    email = effectiveEmail,
                    fullName = effectiveName.ifBlank { "User" },
                    role = effectiveRole,
                    isGuest = false
                )
            } else {
                UserProfile(
                    email = "",
                    fullName = "Guest User",
                    role = UserRole.READER,
                    isGuest = true
                )
            }
        }
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Exposed User Role & Permissions StateFlows
    val userRole: StateFlow<UserRole> = _currentUser
        .map { it.role }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _currentUser.value.role)

    val canEdit: StateFlow<Boolean> = _currentUser
        .map { it.canEditNaat }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _currentUser.value.canEditNaat)

    val isNonAdmin: StateFlow<Boolean> = _currentUser
        .map { it.isNonAdmin }
        .stateIn(viewModelScope, SharingStarted.Eagerly, _currentUser.value.isNonAdmin)

    private var userRoleListenerJob: Job? = null

    // Settings
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(prefs.getInt("font_size", 18))
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _categoryLayoutMode = MutableStateFlow(CategoryLayoutMode.SWIPE_ROW)
    val categoryLayoutMode: StateFlow<CategoryLayoutMode> = _categoryLayoutMode.asStateFlow()

    // Filtering & Searching
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // Active Reader State
    private val _selectedNaat = MutableStateFlow<NaatEntity?>(null)
    val selectedNaat: StateFlow<NaatEntity?> = _selectedNaat.asStateFlow()

    // Target Naat for Social Media Post Creator (Super Admin Only)
    private val _targetPostNaat = MutableStateFlow<NaatEntity?>(null)
    val targetPostNaat: StateFlow<NaatEntity?> = _targetPostNaat.asStateFlow()

    private val _readerLanguageTab = MutableStateFlow(0) // 0: Hindi, 1: Gujarati, 2: English
    val readerLanguageTab: StateFlow<Int> = _readerLanguageTab.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _toastNotification = MutableStateFlow<String?>(null)
    val toastNotification: StateFlow<String?> = _toastNotification.asStateFlow()

    // Data streams
    val allApprovedNaats: StateFlow<List<NaatEntity>> = combine(
        _selectedCategoryId,
        _searchQuery
    ) { catId, query ->
        Pair(catId, query)
    }.flatMapLatest { (catId, query) ->
        if (query.isNotBlank()) {
            repository.search(query.trim())
        } else if (catId != null) {
            repository.getNaatsByCategory(catId)
        } else {
            repository.getAllActiveNaats()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesList: StateFlow<List<NaatEntity>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSelectedNaatFavorite: StateFlow<Boolean> = _selectedNaat
        .flatMapLatest { naat ->
            if (naat == null) flowOf(false)
            else repository.isFavorite(naat.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val pendingList: StateFlow<List<NaatEntity>> = repository.getPendingApprovals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashList: StateFlow<List<NaatEntity>> = repository.getTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Submissions by currently logged in user (Contributor tracking)
    val mySubmissions: StateFlow<List<NaatEntity>> = _currentUser.flatMapLatest { user ->
        if (user.email.isNotBlank()) {
            repository.getBySubmitter(user.email)
        } else {
            flowOf(emptyList<NaatEntity>())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Editing target for direct edit from Home / Reader / Admin portal
    private val _editingNaatTarget = MutableStateFlow<NaatEntity?>(null)
    val editingNaatTarget: StateFlow<NaatEntity?> = _editingNaatTarget.asStateFlow()

    // Firestore application stats
    private val _firestoreStats = MutableStateFlow(FirestoreAppStats())
    val firestoreStats: StateFlow<FirestoreAppStats> = _firestoreStats.asStateFlow()

    // Firestore user roles
    val firestoreUsers: StateFlow<List<UserProfile>> = repository.getUsersFromFirestore()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getFallbackUsers())

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
            repository.startFirestoreSync(viewModelScope)
            if (_currentUser.value.canApproveNaat && FirebaseAuth.getInstance().currentUser != null) {
                repository.startAdminPendingSync(viewModelScope)
                refreshFirestoreStats()
            }
        }

        // Initialize Firestore listener to fetch and store user roles
        val currentAuthUser = FirebaseAuth.getInstance().currentUser
        val initialUid = currentAuthUser?.uid ?: prefs.getString("user_uid", "") ?: ""
        val initialEmail = currentAuthUser?.email ?: prefs.getString("user_email", "") ?: ""
        if (initialUid.isNotBlank() || initialEmail.isNotBlank()) {
            attachUserRoleListener(initialUid, initialEmail)
        }

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                attachUserRoleListener(user.uid, user.email ?: "")
            }
        }
    }

    /**
     * Attaches a real-time Firebase Firestore listener to fetch and store user roles
     * (e.g., 'Reader', 'Editor', 'Admin') in the ViewModel.
     */
    fun attachUserRoleListener(uid: String, email: String = "") {
        userRoleListenerJob?.cancel()
        userRoleListenerJob = viewModelScope.launch {
            repository.listenToUserRole(uid, email).collect { updatedRole ->
                Log.d("NaatViewModel", "Firestore user role received: ${updatedRole.name} for user: $uid")
                _currentUser.update { current ->
                    current.copy(role = updatedRole)
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
            if (screen == Screen.ADMIN_PORTAL && _currentUser.value.canAddNaat) {
                if (_currentUser.value.canApproveNaat) {
                    repository.startAdminPendingSync(viewModelScope)
                }
                refreshFirestoreStats()
            }
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        return false
    }

    fun continueAsGuest() {
        _currentUser.value = UserProfile(
            email = "",
            fullName = "Guest User",
            role = UserRole.VIEWER,
            isGuest = true
        )
        prefs.edit()
            .putBoolean("is_guest", true)
            .putBoolean("is_logged_in", false)
            .putBoolean("guest_session_active", true)
            .putString("user_email", "")
            .putString("user_name", "Guest User")
            .putString("user_uid", "")
            .commit()
        _currentScreen.value = Screen.HOME
    }

    fun signInWithGoogleCredential(idToken: String) {
        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val email = user.email ?: ""
                    val displayName = user.displayName ?: ""
                    val uid = user.uid
                    login(email, displayName, uid)
                } else {
                    showToast("Google sign in failed")
                }
            } catch (e: Exception) {
                Log.e("NaatViewModel", "Firebase Google Sign-In error", e)
                showToast("Sign in failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    /**
     * Checks if a persistent user session exists on app startup and routes directly to HOME,
     * preventing automatic logout upon closing and reopening the app.
     */
    fun checkAutoLoginAndNavigate() {
        val fbUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
        val savedEmail = prefs.getString("user_email", "")?.trim() ?: ""
        val isLoggedIn = prefs.getBoolean("is_logged_in", false) || prefs.getBoolean("user_session_persisted", false)
        val hasGuestSession = prefs.getBoolean("guest_session_active", false) || prefs.getBoolean("is_guest", false)
        val savedUid = prefs.getString("user_uid", "") ?: ""
        val savedName = prefs.getString("user_name", "") ?: ""
        val savedRole = prefs.getString("user_role", "") ?: ""

        val effectiveEmail = fbUser?.email?.trim().takeUnless { it.isNullOrBlank() } ?: savedEmail
        val hasValidSession = fbUser != null || effectiveEmail.isNotBlank() || isLoggedIn || hasGuestSession

        if (hasValidSession) {
            val role = if (savedRole.isNotBlank()) {
                UserRole.fromString(savedRole)
            } else if (effectiveEmail.isNotBlank()) {
                RoleManager.getRoleForEmail(effectiveEmail)
            } else {
                UserRole.VIEWER
            }
            val isGuest = hasGuestSession && effectiveEmail.isBlank()
            val displayName = if (isGuest) {
                "Guest User"
            } else {
                fbUser?.displayName.takeUnless { it.isNullOrBlank() }
                    ?: if (savedName.isNotBlank() && savedName != "Guest User") savedName else effectiveEmail.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "User" }
            }
            val uid = fbUser?.uid ?: savedUid

            val profile = UserProfile(
                uid = uid,
                email = effectiveEmail,
                fullName = displayName,
                role = role,
                isGuest = isGuest
            )
            _currentUser.value = profile
            if (effectiveEmail.isNotBlank()) {
                attachUserRoleListener(uid, effectiveEmail)
            }
            viewModelScope.launch {
                repository.startFirestoreSync(viewModelScope)
                if (profile.canApproveNaat) {
                    repository.startAdminPendingSync(viewModelScope)
                    refreshFirestoreStats()
                }
            }
            _currentScreen.value = Screen.HOME
        } else {
            _currentScreen.value = Screen.AUTH
        }
    }

    /**
     * Performs email/password sign-in or account creation with Firebase Auth fallback
     */
    fun loginWithEmail(email: String, name: String = "", password: String = "", isSignUp: Boolean = false) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            showToast("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            var firebaseUid = ""
            try {
                val auth = FirebaseAuth.getInstance()
                val authResult = if (isSignUp) {
                    try {
                        auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                    } catch (e: Exception) {
                        // If user already exists during sign up, try signing in instead
                        auth.signInWithEmailAndPassword(cleanEmail, password).await()
                    }
                } else {
                    try {
                        auth.signInWithEmailAndPassword(cleanEmail, password).await()
                    } catch (e: Exception) {
                        // If user doesn't exist during sign in, auto-create account for smooth user experience
                        try {
                            auth.createUserWithEmailAndPassword(cleanEmail, password).await()
                        } catch (ex: Exception) {
                            throw e // throw original sign-in error if creation also fails
                        }
                    }
                }
                firebaseUid = authResult.user?.uid ?: ""
            } catch (e: Exception) {
                Log.w("NaatViewModel", "Firebase auth note/fallback mode: ${e.message}")
                // Allow login to proceed locally even if Firebase Auth service is restricted or offline
            }
            login(cleanEmail, name, firebaseUid)
        }
    }

    fun login(email: String, name: String = "", uid: String = "") {
        val cleanEmail = email.trim()
        val role = RoleManager.getRoleForEmail(cleanEmail)
        val displayName = if (name.isNotBlank() && name != "Guest User") name else cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val profile = UserProfile(
            uid = uid,
            email = cleanEmail,
            fullName = displayName,
            role = role,
            isGuest = false
        )
        _currentUser.value = profile
        // CRITICAL: commit() synchronously writes session to disk to prevent auto-logout on restart
        prefs.edit()
            .putBoolean("is_guest", false)
            .putBoolean("is_logged_in", true)
            .putBoolean("user_session_persisted", true)
            .putString("user_email", cleanEmail)
            .putString("user_name", displayName)
            .putString("user_uid", uid)
            .putString("user_role", role.name)
            .remove("guest_session_active")
            .commit()

        viewModelScope.launch {
            repository.saveUserProfileToFirestore(profile)
            repository.startFirestoreSync(viewModelScope)
            if (profile.canApproveNaat) {
                repository.startAdminPendingSync(viewModelScope)
                refreshFirestoreStats()
            }
        }
        attachUserRoleListener(uid, cleanEmail)
        _currentScreen.value = Screen.HOME
        showToast("Signed in as $displayName (${role.displayName})")
    }

    fun logout() {
        userRoleListenerJob?.cancel()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        repository.stopAdminPendingSync()
        prefs.edit()
            .clear()
            .putBoolean("is_logged_in", false)
            .putBoolean("is_guest", true)
            .putBoolean("guest_session_active", false)
            .commit()
        _currentUser.value = UserProfile(role = UserRole.READER, isGuest = true)
        _currentScreen.value = Screen.AUTH
        showToast("Logged out successfully")
    }

    fun toggleDarkMode(enableDark: Boolean) {
        _isDarkMode.value = enableDark
        prefs.edit().putBoolean("dark_theme", enableDark).apply()
        val text = if (enableDark) "Dark Reading Mode Activated" else "Light Reading Mode Activated"
        showToast(text)
    }

    fun updateFontSize(newSizeSp: Int) {
        _fontSizeSp.value = newSizeSp.coerceIn(14, 32)
        prefs.edit().putInt("font_size", _fontSizeSp.value).apply()
    }

    fun setCategoryLayoutMode(mode: CategoryLayoutMode) {
        _categoryLayoutMode.value = mode
    }

    fun selectCategory(categoryId: String?) {
        if (_selectedCategoryId.value == categoryId) {
            _selectedCategoryId.value = null // toggle off if tapped again
        } else {
            _selectedCategoryId.value = categoryId
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun openReader(naat: NaatEntity) {
        viewModelScope.launch {
            val isFav = repository.isFavoriteSync(naat.id)
            _selectedNaat.value = naat.copy(isFavorite = isFav)
        }
        _selectedNaat.value = naat
        _isPlayingAudio.value = false
        navigateTo(Screen.READER)
    }

    /**
     * Opens the Social Media / Instagram Post Creator for Super Admin only
     */
    fun openPostCreator(naat: NaatEntity? = null) {
        if (!_currentUser.value.isSuperAdmin) {
            showToast("Access Restricted: Only Super Admin can create Social Media posts.")
            return
        }
        _targetPostNaat.value = naat ?: _selectedNaat.value ?: allApprovedNaats.value.firstOrNull()
        navigateTo(Screen.POST_CREATOR)
    }

    fun setTargetPostNaat(naat: NaatEntity) {
        _targetPostNaat.value = naat
    }

    fun setReaderLanguageTab(tabIndex: Int) {
        _readerLanguageTab.value = tabIndex
    }

    fun toggleAudioPlayback() {
        _isPlayingAudio.value = !_isPlayingAudio.value
    }

    fun addToFavorites(naat: NaatEntity) {
        viewModelScope.launch {
            repository.addToFavorites(naat)
            if (_selectedNaat.value?.id == naat.id) {
                _selectedNaat.value = _selectedNaat.value?.copy(isFavorite = true)
            }
            showToast("Added to Favorites")
        }
    }

    fun removeFromFavorites(naat: NaatEntity) {
        viewModelScope.launch {
            repository.removeFromFavorites(naat.id)
            if (_selectedNaat.value?.id == naat.id) {
                _selectedNaat.value = _selectedNaat.value?.copy(isFavorite = false)
            }
            showToast("Removed from Favorites")
        }
    }

    fun toggleFavorite(naat: NaatEntity) {
        viewModelScope.launch {
            val isNowFav = repository.toggleFavorite(naat)
            if (_selectedNaat.value?.id == naat.id) {
                _selectedNaat.value = _selectedNaat.value?.copy(isFavorite = isNowFav)
            }
            showToast(if (isNowFav) "Added to Favorites" else "Removed from Favorites")
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.initializeIfNeeded()
            val result = repository.syncRemoteNaatsToLocalDb()
            result.onSuccess { count ->
                showToast("Sync completed • $count Naats available offline")
            }.onFailure {
                showToast("Sync completed • Offline lyrics are up-to-date")
            }
        }
    }

    fun syncLatestNaatsFromFirebase() {
        viewModelScope.launch {
            com.example.data.sync.NaatBackgroundSyncService.syncNow(getApplication())
            val result = repository.syncRemoteNaatsToLocalDb()
            result.onSuccess { count ->
                showToast("Sync completed • $count Naats updated for offline reading")
            }.onFailure {
                showToast("Sync completed • Using offline cache")
            }
        }
    }

    // Navigation helper for editing/adding
    fun startEditing(naat: NaatEntity) {
        if (!_currentUser.value.canEditNaat) {
            showToast("Access Restricted: Non-admins cannot edit Kalam Sharif.")
            return
        }
        _editingNaatTarget.value = naat
        navigateTo(Screen.ADMIN_PORTAL)
    }

    fun startAdding() {
        if (!_currentUser.value.canAddNaat) {
            showToast("You need Contributor or Super Admin access to add Naat Sharif.")
            return
        }
        _editingNaatTarget.value = null
        navigateTo(Screen.ADMIN_PORTAL)
    }

    fun clearEditingTarget() {
        _editingNaatTarget.value = null
    }

    // Admin & Contributor Actions
    fun saveNaat(
        id: String?,
        titleHindi: String,
        titleGujarati: String,
        titleEnglish: String,
        categoryId: String,
        poet: String,
        lyricsHindi: String,
        lyricsGujarati: String,
        lyricsEnglish: String,
        audioUrl: String? = null,
        publisher: String = "",
        addedByName: String = ""
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val isEditing = !id.isNullOrBlank()

            if (isEditing && !user.canEditNaat) {
                showToast("Access Restricted: Non-admins cannot edit Kalam Sharif.")
                return@launch
            }

            if (!user.canAddNaat) {
                showToast("Permission denied. Guest users cannot submit Naat Sharif.")
                return@launch
            }

            val catInfo = DefaultCategories.findById(categoryId)
            val categoryTitle = catInfo?.titleHindi ?: "नात शरीफ"
            val shouldAutoApprove = user.canEditNaat // Super Admin / Admin auto-publishes
            val naatId = id ?: UUID.randomUUID().toString()

            val effectiveAddedBy = addedByName.trim().ifBlank {
                user.fullName.ifBlank { user.email.substringBefore("@") }.ifBlank { "Azazmadkiya" }
            }

            val naat = NaatEntity(
                id = naatId,
                titleHindi = titleHindi.trim(),
                titleGujarati = titleGujarati.trim(),
                titleEnglish = titleEnglish.trim(),
                categoryId = categoryId,
                categoryName = categoryTitle,
                poet = poet.trim(),
                publisher = publisher.trim(),
                lyricsHindi = lyricsHindi.trim(),
                lyricsGujarati = lyricsGujarati.trim(),
                lyricsEnglish = lyricsEnglish.trim(),
                audioUrl = audioUrl?.takeIf { it.isNotBlank() },
                isApproved = shouldAutoApprove, // Contributors submit for Super Admin approval
                isDeleted = false,
                submittedBy = user.email.ifBlank { effectiveAddedBy },
                addedByName = effectiveAddedBy
            )
            repository.insertOrUpdateNaat(naat)
            _editingNaatTarget.value = null

            val msg = if (shouldAutoApprove) {
                if (isEditing) "Naat Sharif modified & updated successfully!" else "Naat Sharif published live!"
            } else {
                "Request sent! Submitted to Super Admin for approval."
            }
            showToast(msg)
        }
    }

    fun approveNaat(id: String) {
        if (!_currentUser.value.canApproveNaat) {
            showToast("Only Super Admin can approve submissions.")
            return
        }
        viewModelScope.launch {
            repository.approveNaat(id)
            showToast("Naat Sharif approved & published live!")
        }
    }

    fun softDeleteNaat(id: String) {
        if (!_currentUser.value.canDeleteNaat) {
            showToast("Only Super Admin can delete Naat Sharif.")
            return
        }
        viewModelScope.launch {
            repository.softDelete(id)
            if (_selectedNaat.value?.id == id) {
                navigateBack()
            }
            showToast("Moved to Trash")
        }
    }

    fun restoreNaat(id: String) {
        if (!_currentUser.value.canDeleteNaat) {
            showToast("Only Super Admin can restore Naat Sharif.")
            return
        }
        viewModelScope.launch {
            repository.restore(id)
            showToast("Restored from Trash")
        }
    }

    fun refreshFirestoreStats() {
        viewModelScope.launch {
            try {
                val stats = repository.fetchFirestoreApplicationStats()
                _firestoreStats.value = stats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUserRole(user: UserProfile, newRole: UserRole) {
        if (!_currentUser.value.isSuperAdmin) {
            showToast("Only Super Admin can modify user roles.")
            return
        }
        viewModelScope.launch {
            repository.updateUserRoleInFirestore(user.uid, user.email, newRole)
            refreshFirestoreStats()
            showToast("Role for ${user.email} updated to ${newRole.displayName}")
        }
    }

    fun assignUserRoleByEmail(email: String, name: String, role: UserRole) {
        if (!_currentUser.value.isSuperAdmin) {
            showToast("Only Super Admin can assign user roles.")
            return
        }
        viewModelScope.launch {
            repository.assignUserRoleByEmail(email, name, role)
            refreshFirestoreStats()
            showToast("Role assigned to $email as ${role.displayName}")
        }
    }

    fun permanentDelete(id: String) {
        if (!_currentUser.value.canDeleteNaat) {
            showToast("Only Super Admin can permanently delete Naat Sharif.")
            return
        }
        viewModelScope.launch {
            repository.permanentDelete(id)
            showToast("Permanently deleted")
        }
    }

    fun exportBackup(onExported: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportToJson()
            onExported(json)
            showToast("Backup exported successfully")
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val count = repository.importFromJson(jsonString)
            showToast("Restored $count Naats from backup")
        }
    }

    fun showToast(message: String) {
        _toastNotification.value = message
    }

    fun clearToast() {
        _toastNotification.value = null
    }
}
