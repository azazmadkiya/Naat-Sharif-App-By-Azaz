package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NaatEntity
import com.example.data.model.DefaultCategories
import com.example.data.model.UserRole
import com.example.ui.components.CategoryGridCard
import com.example.ui.components.CategoryRowCard
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.viewmodel.CategoryLayoutMode
import com.example.ui.viewmodel.NaatViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(viewModel: NaatViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val naats by viewModel.allApprovedNaats.collectAsState()
    val categoryMode by viewModel.categoryLayoutMode.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val toastMessage by viewModel.toastNotification.collectAsState()

    val canAccessAdmin = currentUser.canAddNaat
    val isSuperAdmin = currentUser.canEditNaat
    val isContributor = currentUser.isContributor

    var itemToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<NaatEntity?>(null) }

    var hasNotificationPermission by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(com.example.util.NotificationHelper.isPermissionGranted(context))
    }
    var showNotificationBanner by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(!hasNotificationPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
    }

    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        showNotificationBanner = !isGranted
        if (isGranted) {
            com.example.util.NotificationHelper.sendNotification(context)
            android.widget.Toast.makeText(context, "Notifications enabled! You will receive daily Durood reminders.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val activity = context as? androidx.activity.ComponentActivity
    androidx.activity.compose.BackHandler {
        if (isSearchActive) {
            viewModel.toggleSearch(false)
        } else if (selectedCatId != null) {
            viewModel.selectCategory(null)
        } else {
            activity?.moveTaskToBack(true)
        }
    }

    Scaffold(
        floatingActionButton = {
            if (canAccessAdmin) {
                if (isSuperAdmin) {
                    androidx.compose.material3.ExtendedFloatingActionButton(
                        onClick = { viewModel.navigateTo(Screen.ADMIN_PORTAL) },
                        containerColor = IslamicEmerald,
                        contentColor = Color.White,
                        icon = {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Portal",
                                tint = IslamicGoldBright
                            )
                        },
                        text = {
                            Text(
                                "Admin Portal",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        modifier = Modifier.testTag("admin_portal_fab")
                    )
                } else if (isContributor) {
                    androidx.compose.material3.ExtendedFloatingActionButton(
                        onClick = { viewModel.startAdding() },
                        containerColor = IslamicGoldBright,
                        contentColor = Color.Black,
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Submit Naat",
                                tint = Color.Black
                            )
                        },
                        text = {
                            Text(
                                "+ Submit Naat",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        },
                        modifier = Modifier.testTag("contributor_submit_fab")
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Top Custom Emerald App Bar (Matching Screenshot)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IslamicEmeraldDark)
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Naat Sharif Logo",
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "नात शरीफ",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Naat & Kalam Lyrics",
                                        fontSize = 11.sp,
                                        color = IslamicGoldBright,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Action Icons: Elegantly sized to fit perfectly on every device without clipping
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // 1. Search
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSearchActive) IslamicGoldBright.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                                        .clickable { viewModel.toggleSearch(!isSearchActive) }
                                        .testTag("home_search_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = if (isSearchActive) IslamicGoldBright else Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 2. Favorites
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable { viewModel.navigateTo(Screen.FAVORITES) }
                                        .testTag("home_favorites_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorites",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 3. Settings (Featured prominently in Gold - clearly visible)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGoldBright.copy(alpha = 0.22f))
                                        .clickable { viewModel.navigateTo(Screen.SETTINGS) }
                                        .testTag("home_settings_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = IslamicGoldBright,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 4. Theme Toggle
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable { viewModel.toggleDarkMode(!isDarkMode) }
                                        .testTag("home_theme_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                                        contentDescription = "Toggle Theme",
                                        tint = if (isDarkMode) IslamicGoldBright else Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                // 5. Sync/Refresh
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable { viewModel.refreshData() }
                                        .testTag("home_refresh_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }

                        // Search Field Bar
                        AnimatedVisibility(visible = isSearchActive) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    placeholder = { Text("Search by title, poet or lyrics...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = IslamicEmerald)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedBorderColor = IslamicGoldBright,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("home_search_input")
                                )
                            }
                        }
                    }
                }

                // Notification Permission Banner (when permission not yet granted on Android 13+)
                if (showNotificationBanner) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFFFF3E0)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .testTag("notification_permission_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFF9800).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Enable Notifications / नोटिफिकेशन चालू करें",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Get daily blessed Durood Sharif & Jummah alerts",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                androidx.compose.material3.Button(
                                    onClick = {
                                        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = IslamicEmerald
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = "Allow",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = { showNotificationBanner = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Categories Header & View Mode Switcher
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Categories / કેટેગરીઝ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "8 Categories available",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented Layout Mode Switcher (Matching screenshot 2 & 7)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Swipe Row Button
                                val isSwipe = categoryMode == CategoryLayoutMode.SWIPE_ROW
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSwipe) IslamicGoldBright else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setCategoryLayoutMode(CategoryLayoutMode.SWIPE_ROW) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.StayCurrentPortrait,
                                            contentDescription = null,
                                            tint = if (isSwipe) Color(0xFF1E1700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Swipe Row",
                                            color = if (isSwipe) Color(0xFF1E1700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                // Grid View Button
                                val isGrid = categoryMode == CategoryLayoutMode.GRID_VIEW
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isGrid) IslamicGoldBright else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setCategoryLayoutMode(CategoryLayoutMode.GRID_VIEW) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.GridView,
                                            contentDescription = null,
                                            tint = if (isGrid) Color(0xFF1E1700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Grid View",
                                            color = if (isGrid) Color(0xFF1E1700) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Items (Swipe Row vs Grid View)
                if (categoryMode == CategoryLayoutMode.SWIPE_ROW) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(DefaultCategories.ALL) { cat ->
                                val count = naats.count { it.categoryId == cat.id }
                                val isSelected = selectedCatId == cat.id
                                CategoryRowCard(
                                    category = cat,
                                    count = count,
                                    isSelected = isSelected,
                                    onClick = { viewModel.selectCategory(cat.id) }
                                )
                            }
                        }
                    }
                } else {
                    // Grid View: 2 items per row
                    val chunks = DefaultCategories.ALL.chunked(2)
                    items(chunks) { rowCats ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (cat in rowCats) {
                                val count = naats.count { it.categoryId == cat.id }
                                val isSelected = selectedCatId == cat.id
                                Box(modifier = Modifier.weight(1f)) {
                                    CategoryGridCard(
                                        category = cat,
                                        count = count,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectCategory(cat.id) }
                                    )
                                }
                            }
                            if (rowCats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Kalam List Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        val headerTitle = if (selectedCatId != null) {
                            val cat = DefaultCategories.findById(selectedCatId!!)
                            "${cat?.titleHindi ?: "Naat"} / ${cat?.titleGujarati ?: ""}"
                        } else {
                            "All Naat Sharif & Kalam Sharif / તમામ કલામ"
                        }
                        Text(
                            text = headerTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${naats.size} Naat Sharif total",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Empty state or Kalam items
                if (naats.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No Kalam found matching your search.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(naats, key = { it.id }) { naat ->
                        NaatListItemCard(
                            naat = naat,
                            canEdit = isSuperAdmin,
                            canDelete = isSuperAdmin,
                            canCreatePost = currentUser.isSuperAdmin,
                            onClick = { viewModel.openReader(naat) },
                            onToggleFavorite = { viewModel.toggleFavorite(naat) },
                            onEdit = { viewModel.startEditing(naat) },
                            onDelete = { itemToDelete = naat },
                            onCreatePost = { viewModel.openPostCreator(naat) }
                        )
                    }
                }
            }

            // Bottom Toast/Banner (Matching reference screenshot: "🌙 Dark Reading Mode Activated")
            if (toastMessage != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .clickable { viewModel.clearToast() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🇮🇳 ", fontSize = 14.sp)
                        Text(
                            text = toastMessage ?: "",
                            color = Color(0xFF141C17),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Super Admin Delete Confirmation Dialog
            if (itemToDelete != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { itemToDelete = null },
                    title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Are you sure you want to move \"${itemToDelete?.titleHindi}\" to Trash? (Super Admin can restore it anytime).")
                    },
                    confirmButton = {
                        androidx.compose.material3.Button(
                            onClick = {
                                itemToDelete?.let { viewModel.softDeleteNaat(it.id) }
                                itemToDelete = null
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                        ) {
                            Text("Move to Trash")
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { itemToDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NaatListItemCard(
    naat: NaatEntity,
    canEdit: Boolean = false,
    canDelete: Boolean = false,
    canCreatePost: Boolean = false,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCreatePost: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("naat_card_${naat.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = naat.titleHindi,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = naat.titleGujarati,
                        fontSize = 13.sp,
                        color = IslamicEmerald,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = naat.titleEnglish,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite Button
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (naat.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (naat.isFavorite) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Added by subtitle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Added by: ${naat.displayAddedBy}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IslamicEmerald
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = naat.categoryName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Poet Badge
                    if (naat.poet.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(
                                    IslamicGoldBright.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = naat.poet,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Action Icons (Share, Super Admin Edit/Delete, Play)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Super Admin Edit
                    if (canEdit) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Kalam",
                                tint = IslamicEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Super Admin Delete
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Trash Kalam",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Super Admin Create Social Media / Instagram Post
                    if (canCreatePost) {
                        IconButton(
                            onClick = onCreatePost,
                            modifier = Modifier.size(32.dp).testTag("card_create_post_${naat.id}")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Create Instagram Post",
                                tint = IslamicGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Read Kalam",
                            tint = IslamicEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
