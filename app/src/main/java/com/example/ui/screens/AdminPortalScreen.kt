package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.NaatEntity
import com.example.data.model.DefaultCategories
import com.example.data.model.FirestoreAppStats
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.viewmodel.NaatViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(viewModel: NaatViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allNaats by viewModel.allApprovedNaats.collectAsState()
    val pendingList by viewModel.pendingList.collectAsState()
    val trashList by viewModel.trashList.collectAsState()
    val mySubmissions by viewModel.mySubmissions.collectAsState()
    val editingTarget by viewModel.editingNaatTarget.collectAsState()
    val firestoreStats by viewModel.firestoreStats.collectAsState()
    val firestoreUsers by viewModel.firestoreUsers.collectAsState()

    val isSuperAdmin = currentUser.canEditNaat
    val isContributor = currentUser.isContributor

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isAddingOrEditing by remember { mutableStateOf(false) }
    var editingNaatId by remember { mutableStateOf<String?>(null) }

    // Form Fields
    var titleHindi by remember { mutableStateOf("") }
    var titleGujarati by remember { mutableStateOf("") }
    var titleEnglish by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("naat") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var poet by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var addedByName by remember { mutableStateOf("") }
    var lyricsHindi by remember { mutableStateOf("") }
    var lyricsGujarati by remember { mutableStateOf("") }
    var lyricsEnglish by remember { mutableStateOf("") }

    var showBackupDialog by remember { mutableStateOf(false) }
    var showAssignRoleDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    // Refresh Firestore stats whenever admin enters
    LaunchedEffect(Unit) {
        viewModel.refreshFirestoreStats()
    }

    // Pre-populate when an edit target is passed
    LaunchedEffect(editingTarget) {
        if (editingTarget != null) {
            val item = editingTarget!!
            editingNaatId = item.id
            titleHindi = item.titleHindi
            titleGujarati = item.titleGujarati
            titleEnglish = item.titleEnglish
            selectedCategoryId = item.categoryId
            poet = item.poet
            publisher = item.publisher
            addedByName = item.displayAddedBy
            lyricsHindi = item.lyricsHindi
            lyricsGujarati = item.lyricsGujarati
            lyricsEnglish = item.lyricsEnglish
            isAddingOrEditing = true
        }
    }

    BackHandler {
        if (isAddingOrEditing) {
            isAddingOrEditing = false
            viewModel.clearEditingTarget()
        } else {
            viewModel.navigateBack()
        }
    }

    // SECURITY GATE: If user is neither Super Admin, Admin, nor Contributor, restrict access
    if (!currentUser.canAddNaat) {
        AccessRestrictedView(
            currentUser = currentUser,
            onBack = { viewModel.navigateBack() }
        )
        return
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslamicEmeraldDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (isAddingOrEditing) {
                                isAddingOrEditing = false
                                viewModel.clearEditingTarget()
                            } else {
                                viewModel.navigateBack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = IslamicGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isContributor) "Contributor Portal" else "Secure Admin Dashboard",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Logged in: ${currentUser.email.ifBlank { currentUser.fullName }} (${currentUser.role.displayName})",
                                fontSize = 11.sp,
                                color = IslamicGoldBright
                            )
                        }
                    }

                    // Action button in Top Bar
                    if (!isAddingOrEditing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                                modifier = Modifier.testTag("admin_top_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = IslamicGoldBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    editingNaatId = null
                                    titleHindi = ""
                                    titleGujarati = ""
                                    titleEnglish = ""
                                    poet = ""
                                    publisher = ""
                                    addedByName = currentUser.fullName.ifBlank { currentUser.email.substringBefore("@") }.ifBlank { "Azazmadkiya" }
                                    lyricsHindi = ""
                                    lyricsGujarati = ""
                                    lyricsEnglish = ""
                                    isAddingOrEditing = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldBright),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("admin_top_add_button")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isContributor) "Submit Naat" else "Add Kalam",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isAddingOrEditing) {
                // Add / Edit Kalam Form
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    if (isContributor) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E6)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                                .padding(bottom = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB78103),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Contributor Submission Flow",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                    Text(
                                        text = "Your submission will be saved in Firebase Firestore and sent to Super Admin (azazmadkiya@gmail.com) for verification and live publishing.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6D4C41)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = when {
                            editingNaatId != null -> "Edit & Modify Kalam Sharif (Super Admin)"
                            isContributor -> "Submit New Kalam for Super Admin Approval"
                            else -> "Add New Kalam Sharif (Instant Firestore Publish)"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmerald
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Title Hindi
                    OutlinedTextField(
                        value = titleHindi,
                        onValueChange = { titleHindi = it },
                        label = { Text("Title in Hindi (शीर्षक) *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_hindi")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title Gujarati
                    OutlinedTextField(
                        value = titleGujarati,
                        onValueChange = { titleGujarati = it },
                        label = { Text("Title in Gujarati (ગુજરાતી શીર્ષક)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_gujarati")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title English / Roman
                    OutlinedTextField(
                        value = titleEnglish,
                        onValueChange = { titleEnglish = it },
                        label = { Text("Title in English / Roman Urdu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_title_english")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = DefaultCategories.findById(selectedCategoryId)?.titleHindi ?: "नात शरीफ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category / वर्ग *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            DefaultCategories.ALL.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.titleHindi} (${cat.titleEnglish})") },
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Poet Name
                    OutlinedTextField(
                        value = poet,
                        onValueChange = { poet = it },
                        label = { Text("Poet / शायर (उदा. आला हज़रत, मुज़फ़्फ़र वारसी) *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_poet")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Added by / Submitter Name
                    OutlinedTextField(
                        value = addedByName,
                        onValueChange = { addedByName = it },
                        label = { Text("Added by / जोड़ने वाले का नाम (उदा. Azazmadkiya)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_added_by")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lyrics Hindi
                    OutlinedTextField(
                        value = lyricsHindi,
                        onValueChange = { lyricsHindi = it },
                        label = { Text("Complete Lyrics in Hindi (हिंदी शायरी) *") },
                        minLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_lyrics_hindi")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lyrics Gujarati
                    OutlinedTextField(
                        value = lyricsGujarati,
                        onValueChange = { lyricsGujarati = it },
                        label = { Text("Lyrics in Gujarati (ગુજરાતી શાયરી)") },
                        minLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_lyrics_gujarati")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Lyrics English
                    OutlinedTextField(
                        value = lyricsEnglish,
                        onValueChange = { lyricsEnglish = it },
                        label = { Text("Lyrics in English / Roman Urdu") },
                        minLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_lyrics_english")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit & Cancel Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAddingOrEditing = false
                                viewModel.clearEditingTarget()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (titleHindi.isBlank() || lyricsHindi.isBlank()) {
                                    Toast.makeText(context, "Please enter at least Hindi Title and Lyrics", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.saveNaat(
                                    id = editingNaatId,
                                    titleHindi = titleHindi,
                                    titleGujarati = titleGujarati,
                                    titleEnglish = titleEnglish,
                                    categoryId = selectedCategoryId,
                                    poet = poet.ifBlank { "Unknown" },
                                    lyricsHindi = lyricsHindi,
                                    lyricsGujarati = lyricsGujarati,
                                    lyricsEnglish = lyricsEnglish,
                                    audioUrl = null,
                                    publisher = publisher,
                                    addedByName = addedByName
                                )
                                isAddingOrEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_submit_button")
                        ) {
                            Text(if (isContributor) "Submit to Cloud" else "Save & Publish")
                        }
                    }
                }
            } else if (isContributor && !isSuperAdmin) {
                // Contributor Portal (Submissions & Guidelines)
                ContributorPortalContent(
                    mySubmissions = mySubmissions,
                    onAddNew = {
                        editingNaatId = null
                        titleHindi = ""
                        titleGujarati = ""
                        titleEnglish = ""
                        poet = ""
                        publisher = ""
                        addedByName = currentUser.fullName.ifBlank { currentUser.email.substringBefore("@") }.ifBlank { "Azazmadkiya" }
                        lyricsHindi = ""
                        lyricsGujarati = ""
                        lyricsEnglish = ""
                        isAddingOrEditing = true
                    }
                )
            } else {
                // Super Admin Exclusive Feature: Social Media / Instagram Post Studio
                if (currentUser.isSuperAdmin) {
                    Card(
                        onClick = { viewModel.openPostCreator() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("admin_open_post_creator_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = IslamicEmerald.copy(alpha = 0.12f)),
                        border = BorderStroke(1.2.dp, IslamicGoldBright)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(IslamicEmerald, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = IslamicGoldBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📸 Instagram Post Studio (Super Admin)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Create 1:1, 4:5, 9:16 posts with custom fonts, auto-fit & ornate backgrounds",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = IslamicEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // FULL SUPER ADMIN / ADMIN DASHBOARD
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    edgePadding = 12.dp
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Overview & Stats", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pending (${pendingList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("User Roles (${firestoreUsers.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 3,
                        onClick = { selectedTabIndex = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trash (${trashList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                when (selectedTabIndex) {
                    0 -> {
                        // TAB 0: Firestore Application Statistics & Overview
                        FirestoreStatsOverviewTab(
                            stats = firestoreStats,
                            allNaats = allNaats,
                            onRefreshStats = { viewModel.refreshFirestoreStats() },
                            onEdit = { item ->
                                editingNaatId = item.id
                                titleHindi = item.titleHindi
                                titleGujarati = item.titleGujarati
                                titleEnglish = item.titleEnglish
                                selectedCategoryId = item.categoryId
                                poet = item.poet
                                lyricsHindi = item.lyricsHindi
                                lyricsGujarati = item.lyricsGujarati
                                lyricsEnglish = item.lyricsEnglish
                                isAddingOrEditing = true
                            },
                            onDelete = { viewModel.softDeleteNaat(it.id) },
                            onOpenBackup = { showBackupDialog = true }
                        )
                    }

                    1 -> {
                        // TAB 1: Pending Naat Submissions Review
                        PendingSubmissionsTab(
                            pendingList = pendingList,
                            onApprove = { viewModel.approveNaat(it.id) },
                            onReject = { viewModel.softDeleteNaat(it.id) },
                            onEdit = { item ->
                                editingNaatId = item.id
                                titleHindi = item.titleHindi
                                titleGujarati = item.titleGujarati
                                titleEnglish = item.titleEnglish
                                selectedCategoryId = item.categoryId
                                poet = item.poet
                                lyricsHindi = item.lyricsHindi
                                lyricsGujarati = item.lyricsGujarati
                                lyricsEnglish = item.lyricsEnglish
                                isAddingOrEditing = true
                            }
                        )
                    }

                    2 -> {
                        // TAB 2: User Roles Management (Stored in Firebase Firestore)
                        UserRolesManagementTab(
                            users = firestoreUsers,
                            onUpdateRole = { user, newRole ->
                                viewModel.updateUserRole(user, newRole)
                            },
                            onOpenAssignDialog = { showAssignRoleDialog = true }
                        )
                    }

                    3 -> {
                        // TAB 3: Trash & Data Backup
                        TrashAndBackupTab(
                            trashList = trashList,
                            onRestore = { viewModel.restoreNaat(it.id) },
                            onPurge = { viewModel.permanentDelete(it.id) },
                            onExportBackup = {
                                viewModel.exportBackup { json ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Naat Sharif Backup", json))
                                    Toast.makeText(context, "Full JSON backup copied to clipboard (${json.length} characters)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onOpenImportDialog = { showBackupDialog = true }
                        )
                    }
                }
            }
        }

        // Assign Role Dialog
        if (showAssignRoleDialog) {
            AssignRoleDialog(
                onDismiss = { showAssignRoleDialog = false },
                onAssign = { email, name, role ->
                    viewModel.assignUserRoleByEmail(email, name, role)
                    showAssignRoleDialog = false
                }
            )
        }

        // Backup Import Dialog
        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Import JSON Backup", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Paste your exported JSON content below to restore all records:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text("[ { \"titleHindi\": \"...\" } ]") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importJsonText.isNotBlank()) {
                                viewModel.importBackup(importJsonText)
                                showBackupDialog = false
                                importJsonText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                    ) {
                        Text("Import & Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ACCESS RESTRICTED VIEW (SECURITY GATE)
// -------------------------------------------------------------------------------------------------
@Composable
private fun AccessRestrictedView(
    currentUser: UserProfile,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslamicEmeraldDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Admin Access Verification", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFFFFEBEE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Access Restricted",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This Admin Dashboard is secured for Super Admin (azazmadkiya@gmail.com) and authorized contributors to manage Naat Sharif content, approve submissions, and configure user permissions.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Current Signed-In Account:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser.email.ifBlank { "Guest / Anonymous User" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Role: ${currentUser.role.displayName}",
                                fontSize = 11.sp,
                                color = IslamicEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Return to Home Screen")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 0: FIRESTORE APPLICATION STATISTICS & OVERVIEW
// -------------------------------------------------------------------------------------------------
@Composable
private fun FirestoreStatsOverviewTab(
    stats: FirestoreAppStats,
    allNaats: List<NaatEntity>,
    onRefreshStats: () -> Unit,
    onEdit: (NaatEntity) -> Unit,
    onDelete: (NaatEntity) -> Unit,
    onOpenBackup: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(stats.lastSyncTime))

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Live Firestore Status Bar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (stats.isLiveFromFirestore) IslamicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, if (stats.isLiveFromFirestore) IslamicEmerald else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (stats.isLiveFromFirestore) Color(0xFF2E7D32) else Color(0xFFF57F17), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (stats.isLiveFromFirestore) "Firestore Cloud Connected (Live)" else "Local Database Synced",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Last updated: $formattedTime • Auto-synced with Google Cloud",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshStats,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Stats", tint = IslamicEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Application Statistics Grid
            Text(
                text = "Application Statistics (Firebase Firestore)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Published Kalam",
                    value = "${stats.totalApproved}",
                    subtitle = "Live on App",
                    color = IslamicEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Review",
                    value = "${stats.pendingReview}",
                    subtitle = "Needs Approval",
                    color = Color(0xFFD48B00),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Trash / Rejected",
                    value = "${stats.trashedCount}",
                    subtitle = "Soft Deleted",
                    color = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Registered Users",
                    value = "${stats.totalUsers}",
                    subtitle = "Cloud Profiles",
                    color = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Contributors & Admins",
                    value = "${stats.contributorsCount}",
                    subtitle = "Authorized Team",
                    color = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Breakdown Chips
            Text(
                text = "Content by Category",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(stats.categoryCounts.toList()) { (catName, count) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(catName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(IslamicEmerald, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("$count", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Published Kalam Directory (${allNaats.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap ✏️ to edit or 🗑️ to trash",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(allNaats, key = { it.id }) { item ->
            AdminKalamCard(
                naat = item,
                canEdit = true,
                canDelete = true,
                onEdit = { onEdit(item) },
                onDelete = { onDelete(item) }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 1: PENDING SUBMISSIONS REVIEW
// -------------------------------------------------------------------------------------------------
@Composable
private fun PendingSubmissionsTab(
    pendingList: List<NaatEntity>,
    onApprove: (NaatEntity) -> Unit,
    onReject: (NaatEntity) -> Unit,
    onEdit: (NaatEntity) -> Unit
) {
    if (pendingList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IslamicEmerald,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "All Caught Up! ✨",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No pending submissions awaiting review. New contributor submissions will automatically appear here via Firestore sync.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFB78103))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${pendingList.size} Kalam submission(s) awaiting your review and approval before publishing live to users.",
                            fontSize = 12.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }

            items(pendingList, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.titleHindi,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFFECB3), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("Awaiting Approval", color = Color(0xFFB78103), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (item.titleGujarati.isNotBlank() || item.titleEnglish.isNotBlank()) {
                            Text(
                                text = listOf(item.titleGujarati, item.titleEnglish).filter { it.isNotBlank() }.joinToString(" • "),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(IslamicEmerald.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(item.categoryName, color = IslamicEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Poet: ${item.poet}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Added by: ${item.displayAddedBy}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = item.lyricsHindi.take(180) + if (item.lyricsHindi.length > 180) "..." else "",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onReject(item) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { onEdit(item) },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit First", fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onApprove(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve & Publish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 2: USER ROLES MANAGEMENT (FIRESTORE DATA)
// -------------------------------------------------------------------------------------------------
@Composable
private fun UserRolesManagementTab(
    users: List<UserProfile>,
    onUpdateRole: (UserProfile, UserRole) -> Unit,
    onOpenAssignDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Firebase Firestore User Roles",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Roles and permissions are synchronized live in Firestore (/users collection).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenAssignDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Role", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Registered Users & Contributors (${users.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(users, key = { it.uid.ifBlank { it.email } }) { user ->
            UserRoleItemCard(
                user = user,
                onSelectRole = { newRole ->
                    onUpdateRole(user, newRole)
                }
            )
        }
    }
}

@Composable
private fun UserRoleItemCard(
    user: UserProfile,
    onSelectRole: (UserRole) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val roleColor = when (user.role) {
        UserRole.SUPER_ADMIN -> IslamicGoldBright
        UserRole.ADMIN -> IslamicEmerald
        UserRole.EDITOR -> Color(0xFF00897B)
        UserRole.CONTRIBUTOR -> Color(0xFF1976D2)
        UserRole.READER -> MaterialTheme.colorScheme.outline
        UserRole.VIEWER -> MaterialTheme.colorScheme.outline
    }

    val roleIcon = when (user.role) {
        UserRole.SUPER_ADMIN -> Icons.Default.WorkspacePremium
        UserRole.ADMIN -> Icons.Default.Security
        UserRole.EDITOR -> Icons.Default.Edit
        UserRole.CONTRIBUTOR -> Icons.Default.Add
        UserRole.READER -> Icons.Default.MenuBook
        UserRole.VIEWER -> Icons.Default.MenuBook
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(roleColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(roleIcon, contentDescription = null, tint = roleColor, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.fullName.ifBlank { "User" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email.ifBlank { "UID: ${user.uid.take(10)}..." },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = user.role.name.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    UserRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.displayName, fontSize = 13.sp) },
                            onClick = {
                                expanded = false
                                onSelectRole(role)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TAB 3: TRASH & BACKUP
// -------------------------------------------------------------------------------------------------
@Composable
private fun TrashAndBackupTab(
    trashList: List<NaatEntity>,
    onRestore: (NaatEntity) -> Unit,
    onPurge: (NaatEntity) -> Unit,
    onExportBackup: () -> Unit,
    onOpenImportDialog: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            // Data Backup card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Backup & Disaster Recovery (JSON)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IslamicEmerald
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export the entire database for offline safety, or import and restore records instantly.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onExportBackup,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenImportDialog,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Soft-Deleted Trash Bin (${trashList.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (trashList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Trash bin is empty", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            items(trashList, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.titleHindi, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("${item.categoryName} • Poet: ${item.poet}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Row {
                            IconButton(onClick = { onRestore(item) }) {
                                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = IslamicEmerald)
                            }
                            IconButton(onClick = { onPurge(item) }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Purge Permanently", tint = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// CONTRIBUTOR VIEW CONTENT (SUBMITTER ONLY)
// -------------------------------------------------------------------------------------------------
@Composable
private fun ContributorPortalContent(
    mySubmissions: List<NaatEntity>,
    onAddNew: () -> Unit
) {
    if (mySubmissions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = IslamicEmerald, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Submissions Yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap '+ Submit Naat' above to add a new Naat Sharif and send it to Super Admin for approval.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "My Submissions History (${mySubmissions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(mySubmissions, key = { it.id }) { item ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.titleHindi, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                            val statusColor = when {
                                item.isApproved -> Color(0xFF2E7D32)
                                item.isDeleted -> Color(0xFFC62828)
                                else -> Color(0xFFF57F17)
                            }
                            val statusText = when {
                                item.isApproved -> "Approved & Live"
                                item.isDeleted -> "Rejected"
                                else -> "Pending Review"
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${item.categoryName} • Poet: ${item.poet}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.lyricsHindi.take(120) + "...", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// REUSABLE STAT CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// REUSABLE ADMIN KALAM DIRECTORY CARD
// -------------------------------------------------------------------------------------------------
@Composable
private fun AdminKalamCard(
    naat: NaatEntity,
    canEdit: Boolean,
    canDelete: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(naat.titleHindi, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                if (naat.titleGujarati.isNotBlank()) {
                    Text(naat.titleGujarati, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(naat.categoryName, fontSize = 11.sp, color = IslamicEmerald, fontWeight = FontWeight.SemiBold)
                    Text(" • ${naat.poet}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            Row {
                if (canEdit) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IslamicGoldBright)
                    }
                }
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Move to Trash", tint = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ASSIGN ROLE DIALOG
// -------------------------------------------------------------------------------------------------
@Composable
private fun AssignRoleDialog(
    onDismiss: () -> Unit,
    onAssign: (email: String, name: String, role: UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CONTRIBUTOR) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign User Role in Firestore", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Assign roles to team members. Changes will be saved live in Firebase Firestore.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("User Email (Google Account) *") },
                    placeholder = { Text("user@gmail.com") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selected Role: ${selectedRole.displayName}")
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.displayName) },
                                onClick = {
                                    selectedRole = role
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onAssign(email.trim(), name.trim(), selectedRole)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
            ) {
                Text("Assign Role")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
