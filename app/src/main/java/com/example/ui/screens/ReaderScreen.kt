package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.viewmodel.NaatViewModel

@Composable
fun ReaderScreen(viewModel: NaatViewModel) {
    val naat by viewModel.selectedNaat.collectAsState()
    val activeTab by viewModel.readerLanguageTab.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isFavorite by viewModel.isSelectedNaatFavorite.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    if (naat == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Kalam selected")
        }
        return
    }

    val currentNaat = naat!!

    val displayTitle = when (activeTab) {
        0 -> currentNaat.titleHindi
        1 -> currentNaat.titleGujarati.ifBlank { currentNaat.titleHindi }
        else -> currentNaat.titleEnglish.ifBlank { currentNaat.titleHindi }
    }

    val displayLyrics = when (activeTab) {
        0 -> currentNaat.lyricsHindi
        1 -> currentNaat.lyricsGujarati.ifBlank { currentNaat.lyricsHindi }
        else -> currentNaat.lyricsEnglish.ifBlank { currentNaat.lyricsHindi }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslamicEmeraldDark)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = { viewModel.navigateBack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = displayTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }

                    // Reading Tools (A-, A+, Favorite)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Decrease Font
                        IconButton(
                            onClick = { viewModel.updateFontSize(fontSizeSp - 2) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.TextDecrease,
                                contentDescription = "Decrease Font",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Increase Font
                        IconButton(
                            onClick = { viewModel.updateFontSize(fontSizeSp + 2) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.TextIncrease,
                                contentDescription = "Increase Font",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Favorite Toggle
                        IconButton(
                            onClick = { viewModel.toggleFavorite(currentNaat) },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("add_to_favorites_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                                tint = if (isFavorite) Color(0xFFFF5252) else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Super Admin Edit Kalam
                        if (currentUser.canEditNaat) {
                            IconButton(
                                onClick = { viewModel.startEditing(currentNaat) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Kalam",
                                    tint = IslamicGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Super Admin Delete Kalam
                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Kalam",
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Super Admin Create Instagram / Social Post
                        if (currentUser.isSuperAdmin) {
                            IconButton(
                                onClick = { viewModel.openPostCreator(currentNaat) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("reader_create_post_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Create Instagram Post",
                                    tint = IslamicGoldBright,
                                    modifier = Modifier.size(20.dp)
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
            // Language Switcher Tabs: Hindi, Gujarati, English (Roman Urdu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        "हिंदी (Hindi)",
                        "ગુજરાતી (Gujarati)",
                        "English / Roman"
                    )
                    tabs.forEachIndexed { index, tabTitle ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) IslamicEmerald else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setReaderLanguageTab(index) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Scrollable Lyrics Content Area
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badges
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(IslamicEmerald.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentNaat.categoryName,
                            fontSize = 12.sp,
                            color = IslamicEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .background(IslamicGoldBright.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "शायर: ${currentNaat.poet}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Prominent 'Add to Favorites' interactive button in reader view
                Card(
                    onClick = { viewModel.toggleFavorite(currentNaat) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFavorite) {
                            IslamicEmerald.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isFavorite) IslamicEmerald else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .testTag("reader_favorite_action_chip")
                        .padding(top = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) Color(0xFFE53935) else IslamicEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isFavorite) "Saved in Favorites (पसंदीदा)" else "Add to Favorites (पसंदीदा में जोड़ें)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFavorite) IslamicEmeraldDark else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Super Admin: Create Instagram & Social Post Action Card
                if (currentUser.isSuperAdmin) {
                    Card(
                        onClick = { viewModel.openPostCreator(currentNaat) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = IslamicGoldBright.copy(alpha = 0.16f)),
                        border = BorderStroke(1.2.dp, IslamicGoldBright),
                        modifier = Modifier
                            .testTag("reader_create_instagram_post_card")
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📸 Create Instagram Post (Auto-Fit & Size Options)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) IslamicGoldBright else IslamicEmeraldDark
                            )
                        }
                    }
                }

                // Big Styled Lyrics Card matching user reference screenshot
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Big Title in Reader
                        Text(
                            text = displayTitle,
                            fontSize = (fontSizeSp + 4).sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = (fontSizeSp + 10).sp
                        )

                        if (currentNaat.titleEnglish.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentNaat.titleEnglish.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGoldBright,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 'Added by: <User Add Name>' pill chip matching reference image
                        Box(
                            modifier = Modifier
                                .testTag("reader_added_by_badge")
                                .background(IslamicEmerald.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .border(1.dp, IslamicEmerald.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Added by",
                                    tint = IslamicEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Added by: ${currentNaat.displayAddedBy}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IslamicEmeraldDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Decorative Divider
                        Text(
                            text = "﷽",
                            fontSize = 24.sp,
                            color = IslamicEmerald,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Lyrics Verses
                        Text(
                            text = displayLyrics,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.75f).sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lyrics_reader_content")
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Islamic Bottom Emblem
                        Text(
                            text = "۞ ۞ ۞",
                            fontSize = 18.sp,
                            color = IslamicGoldBright
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Naat Sharif • Hindi & Gujarati Lyrics",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to move \"${currentNaat.titleHindi}\" to Trash? Only Super Admin can restore it.")
                },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.softDeleteNaat(currentNaat.id)
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Move to Trash")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
