package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.viewmodel.NaatViewModel
import com.example.util.ImageFitMode
import com.example.util.PostAspectRatio
import com.example.util.PostImageGenerator
import com.example.util.PostTextAlignment
import com.example.util.PostTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreatorScreen(viewModel: NaatViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val targetNaat by viewModel.targetPostNaat.collectAsState()

    // Enforce Super Admin Only access
    if (!currentUser.isSuperAdmin) {
        BackHandler { viewModel.navigateBack() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Access Restricted: Social Media Post Creator is exclusively available for Super Admin.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.navigateBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Back to Home")
                    }
                }
            }
        }
        return
    }

    BackHandler { viewModel.navigateBack() }

    // ================= STATE CONFIGURATIONS =================
    // 1. Post Size (Aspect Ratio)
    var selectedAspectRatio by remember { mutableStateOf(PostAspectRatio.SQUARE_1_1) }

    // 2. Images Option (Alag Se / Dedicated Image Studio)
    var selectedTheme by remember { mutableStateOf(PostTheme.THEMES[0]) }
    var customImageUri by remember { mutableStateOf<Uri?>(null) }
    var customImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageFitMode by remember { mutableStateOf(ImageFitMode.FULL_FIT_COVER) }
    var imageDimAlpha by remember { mutableFloatStateOf(0.55f) }

    // 3. Text & Auto-Fit Options
    var isAutoFit by remember { mutableStateOf(true) }
    var customFontSizeSp by remember { mutableFloatStateOf(24f) }
    var textAlignment by remember { mutableStateOf(PostTextAlignment.CENTER) }
    var selectedLanguageIndex by remember { mutableIntStateOf(0) } // 0: Hindi, 1: Gujarati, 2: English
    var customTextColor by remember { mutableStateOf(Color.White) }

    // 4. Ornaments & Frames & Footer Credit
    var showBismillah by remember { mutableStateOf(true) }
    var showFrame by remember { mutableStateOf(true) }
    var showPoetBadge by remember { mutableStateOf(true) }
    var showWatermark by remember { mutableStateOf(true) }
    var showFooterCredit by remember { mutableStateOf(true) }
    var footerCreditText by remember { mutableStateOf("/Azazmadkiya") }
    var showFooterSocialIcons by remember { mutableStateOf(true) }
    var footerWithoutBackground by remember { mutableStateOf(false) }
    var footerSizeScale by remember { mutableFloatStateOf(1.0f) }
    var footerVerticalOffset by remember { mutableFloatStateOf(0f) }

    // 5. Text / Lyrics Content
    var customTitle by remember { mutableStateOf("") }
    var customPoet by remember { mutableStateOf("") }
    var customLyrics by remember { mutableStateOf("") }

    // Studio Active Control Tab
    var activeStudioTab by remember { mutableIntStateOf(0) } // 0: Post Size, 1: Images Option, 2: Font & Auto-Fit, 3: Ornaments, 4: Edit Text
    var isExporting by remember { mutableStateOf(false) }

    // Photo Picker launcher (zero permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            coroutineScope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    PostImageGenerator.loadBitmapFromUri(context, uri)
                }
                customImageBitmap = bmp
            }
        }
    }

    // Sync with targetNaat on launch
    LaunchedEffect(targetNaat, selectedLanguageIndex) {
        val naat = targetNaat
        if (naat != null) {
            when (selectedLanguageIndex) {
                0 -> {
                    customTitle = naat.titleHindi
                    customLyrics = naat.lyricsHindi.ifBlank { naat.lyricsEnglish }
                }
                1 -> {
                    customTitle = naat.titleGujarati.ifBlank { naat.titleHindi }
                    customLyrics = naat.lyricsGujarati.ifBlank { naat.lyricsHindi }
                }
                else -> {
                    customTitle = naat.titleEnglish.ifBlank { naat.titleHindi }
                    customLyrics = naat.lyricsEnglish.ifBlank { naat.lyricsHindi }
                }
            }
            customPoet = naat.poet
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Instagram & Social Post Creator",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "👑 Super Admin Exclusive • HD Post Studio",
                            fontSize = 11.sp,
                            color = IslamicGoldBright,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("post_creator_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Quick Action: Copy Caption & Hashtags
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val captionText = "✨ $customTitle\n" +
                                    (if (customPoet.isNotBlank()) "शायर: $customPoet\n\n" else "\n") +
                                    "$customLyrics\n\n" +
                                    "#NaatSharif #KalamSharif #IslamicPost #InstagramPost #IslamicQuotes"
                            val clip = ClipData.newPlainText("Naat Caption", captionText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Caption & hashtags copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Caption",
                            tint = IslamicGoldBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicEmeraldDark)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ================= LIVE VISUAL POST PREVIEW =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val maxPreviewWidth = maxWidth.coerceAtMost(350.dp)
                    val previewHeight = when (selectedAspectRatio) {
                        PostAspectRatio.SQUARE_1_1 -> maxPreviewWidth
                        PostAspectRatio.PORTRAIT_4_5 -> maxPreviewWidth * (5f / 4f)
                        PostAspectRatio.STORY_9_16 -> maxPreviewWidth * (16f / 9f).coerceAtMost(1.5f)
                        PostAspectRatio.LANDSCAPE_16_9 -> maxPreviewWidth * (9f / 16f)
                    }

                    Card(
                        modifier = Modifier
                            .width(maxPreviewWidth)
                            .height(previewHeight)
                            .shadow(14.dp, RoundedCornerShape(16.dp))
                            .testTag("post_live_preview_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(selectedTheme.gradientBottom))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 1. Background (Custom Image OR Gradient)
                            if (customImageBitmap != null) {
                                Image(
                                    bitmap = customImageBitmap!!.asImageBitmap(),
                                    contentDescription = "Custom Background Image",
                                    contentScale = if (imageFitMode == ImageFitMode.FULL_FIT_COVER) ContentScale.Crop else ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dimming overlay for readability
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = imageDimAlpha))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                listOf(
                                                    Color(selectedTheme.gradientTop),
                                                    Color(selectedTheme.gradientBottom)
                                                )
                                            )
                                        )
                                )
                            }

                            // 2. Ornate Frame & Content
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (showFrame) {
                                            Modifier
                                                .padding(7.dp)
                                                .border(
                                                    BorderStroke(2.dp, Color(selectedTheme.goldAccent)),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(3.5.dp)
                                                .border(
                                                    BorderStroke(0.8.dp, Color(selectedTheme.goldAccent).copy(alpha = 0.5f)),
                                                    RoundedCornerShape(8.dp)
                                                )
                                        } else Modifier
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top Area: Bismillah & Title
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (showBismillah) {
                                            Text(
                                                text = "﷽",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(selectedTheme.goldAccent)
                                            )
                                        }

                                        if (customTitle.isNotBlank()) {
                                            Text(
                                                text = customTitle,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(selectedTheme.textSecondary),
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }

                                        if (showPoetBadge && customPoet.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Color(selectedTheme.goldAccent).copy(alpha = 0.2f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .border(
                                                        0.8.dp,
                                                        Color(selectedTheme.goldAccent).copy(alpha = 0.7f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "शायर: $customPoet",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(selectedTheme.goldAccent)
                                                )
                                            }
                                        }
                                    }

                                    // Middle: Lyrics with Auto-Fit or Manual Size
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val previewFontSize = if (isAutoFit) {
                                            when {
                                                customLyrics.length > 300 -> 10.5.sp
                                                customLyrics.length > 180 -> 11.5.sp
                                                customLyrics.length > 90 -> 13.sp
                                                else -> 15.sp
                                            }
                                        } else {
                                            (customFontSizeSp * 0.52f).coerceIn(10f, 22f).sp
                                        }

                                        val previewAlign = when (textAlignment) {
                                            PostTextAlignment.CENTER -> TextAlign.Center
                                            PostTextAlignment.LEFT -> TextAlign.Start
                                            PostTextAlignment.RIGHT -> TextAlign.End
                                        }

                                        Text(
                                            text = customLyrics.ifBlank { "अपने पसंदीदा कलाम के बोल यहाँ दिखाई देंगे..." },
                                            fontSize = previewFontSize,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(selectedTheme.textPrimary),
                                            textAlign = previewAlign,
                                            lineHeight = (previewFontSize.value * 1.38f).sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                    }

                                    // Bottom: Watermark & Footer Credit Preview
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (showWatermark) {
                                            Text(
                                                text = "✦ NAAT SHARIF APP ✦",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(selectedTheme.goldAccent).copy(alpha = 0.9f),
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        if (showFooterCredit) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .then(
                                                        if (!footerWithoutBackground) {
                                                            Modifier
                                                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                                                .border(0.5.dp, Color(selectedTheme.goldAccent).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        } else Modifier
                                                    )
                                            ) {
                                                if (showFooterSocialIcons) {
                                                    // 1. FB
                                                    Box(modifier = Modifier.size(13.dp).background(Color(0xFF1877F2), RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                                                        Text("f", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    // 2. Insta
                                                    Box(modifier = Modifier.size(13.dp).background(Brush.linearGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF))), RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                                                        Text("◎", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    // 3. YouTube
                                                    Box(modifier = Modifier.size(13.dp).background(Color(0xFFFF0000), RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                                                        Text("▶", fontSize = 6.sp, color = Color.White)
                                                    }
                                                    // 4. Play Store
                                                    Box(modifier = Modifier.size(13.dp).background(Color.White, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                                                        Text("▶", fontSize = 6.sp, color = Color(0xFF0080FF), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (footerCreditText.isNotBlank()) {
                                                    Text(
                                                        text = footerCreditText,
                                                        fontSize = (8 * footerSizeScale).coerceIn(6f, 14f).sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Info Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Live Preview: ${selectedAspectRatio.label} • ${if (isAutoFit) "Auto-Fit Text" else "${customFontSizeSp.toInt()}sp"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ================= FEATURE CONTROLS CATEGORY TABS =================
            ScrollableTabRow(
                selectedTabIndex = activeStudioTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = IslamicEmerald,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeStudioTab == 0,
                    onClick = { activeStudioTab = 0 },
                    text = { Text("📐 Post Size", fontWeight = if (activeStudioTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeStudioTab == 1,
                    onClick = { activeStudioTab = 1 },
                    text = { Text("🖼️ Images Option (अलग से)", fontWeight = if (activeStudioTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeStudioTab == 2,
                    onClick = { activeStudioTab = 2 },
                    text = { Text("🔤 Font & Auto-Fit", fontWeight = if (activeStudioTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeStudioTab == 3,
                    onClick = { activeStudioTab = 3 },
                    text = { Text("🎨 Ornaments", fontWeight = if (activeStudioTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeStudioTab == 4,
                    onClick = { activeStudioTab = 4 },
                    text = { Text("✏️ Edit Kalam", fontWeight = if (activeStudioTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ================= ACTIVE TAB CONTENT CARD =================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // TAB 0: POST SIZE OPTIONS
                    if (activeStudioTab == 0) {
                        Text(
                            text = "📐 Post Size / Instagram Format",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Select ideal format for Instagram feed, story, reels, or status",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PostAspectRatio.values().forEach { ratio ->
                                val isSelected = selectedAspectRatio == ratio
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAspectRatio = ratio }
                                        .testTag("post_size_${ratio.name}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) IslamicEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) IslamicEmerald else Color.LightGray.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = ratio.label,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) IslamicEmerald else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${ratio.subtitle} • ${ratio.width}x${ratio.height} px",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = IslamicEmerald,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 1: IMAGES OPTION ALAG SE (DEDICATED IMAGE STUDIO)
                    if (activeStudioTab == 1) {
                        Text(
                            text = "🖼️ Images Option (अलग से / Background Images)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose custom phone gallery photos or Islamic art presets with image adjustments",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // A. Custom Image from Phone Gallery
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "1. Custom Photo from Device (गैलरी से फोटो)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (customImageBitmap != null) "Change Photo" else "Pick Image from Gallery", fontSize = 12.sp)
                                    }

                                    if (customImageBitmap != null) {
                                        OutlinedButton(
                                            onClick = {
                                                customImageUri = null
                                                customImageBitmap = null
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Remove Photo", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // B. Image Fit Mode (Cover / Fit Center)
                        if (customImageBitmap != null) {
                            Text(
                                text = "2. Image Fit Mode (इमेज फिट स्टाइल)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ImageFitMode.values().forEach { mode ->
                                    val isSelected = imageFitMode == mode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { imageFitMode = mode },
                                        label = { Text(mode.label, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = IslamicEmerald,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // C. Image Darkness / Dimming Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Image Darkening (काले रंग की परत): ${(imageDimAlpha * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Text legibility",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Slider(
                                value = imageDimAlpha,
                                onValueChange = { imageDimAlpha = it },
                                valueRange = 0.1f..0.9f,
                                colors = SliderDefaults.colors(
                                    thumbColor = IslamicEmerald,
                                    activeTrackColor = IslamicEmerald
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // D. Preset Islamic Art Themes
                        Text(
                            text = "3. Islamic Preset Art Themes (इस्लामिक थीम)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PostTheme.THEMES.forEach { theme ->
                                val isSelected = selectedTheme.id == theme.id
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { selectedTheme = theme }
                                        .testTag("post_theme_${theme.id}")
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    listOf(Color(theme.gradientTop), Color(theme.gradientBottom))
                                                )
                                            )
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) IslamicGoldBright else Color.Gray.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(theme.goldAccent),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.name,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) IslamicEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // TAB 2: FONT SIZE & AUTO-FIT CONTROLS
                    if (activeStudioTab == 2) {
                        Text(
                            text = "🔤 Font Size & Auto-Fit Text Options",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Adjust text to image automatically full fit or set manual font size",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Auto Fit Text Switch
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAutoFit) IslamicEmerald.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isAutoFit) IslamicEmerald else Color.LightGray.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = IslamicEmerald,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Auto Fit Text to Image (Full Fit)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Automatically adjust text size to fit image canvas",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isAutoFit,
                                    onCheckedChange = { isAutoFit = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = IslamicEmerald,
                                        checkedTrackColor = IslamicEmerald.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.testTag("auto_fit_switch")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Manual Font Size Slider & Steppers
                        AnimatedVisibility(visible = !isAutoFit) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Manual Font Size: ${customFontSizeSp.toInt()} sp",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row {
                                        OutlinedButton(
                                            onClick = { customFontSizeSp = (customFontSizeSp - 2).coerceAtLeast(14f) },
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedButton(
                                            onClick = { customFontSizeSp = (customFontSizeSp + 2).coerceAtMost(56f) },
                                            contentPadding = PaddingValues(horizontal = 10.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("A+", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Slider(
                                    value = customFontSizeSp,
                                    onValueChange = { customFontSizeSp = it },
                                    valueRange = 14f..56f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = IslamicEmerald,
                                        activeTrackColor = IslamicEmerald
                                    ),
                                    modifier = Modifier.testTag("font_size_slider")
                                )

                                // Quick Font Size Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "Small (18sp)" to 18f,
                                        "Medium (24sp)" to 24f,
                                        "Large (32sp)" to 32f,
                                        "Hero (42sp)" to 42f
                                    ).forEach { (label, sz) ->
                                        FilterChip(
                                            selected = customFontSizeSp == sz,
                                            onClick = { customFontSizeSp = sz },
                                            label = { Text(label, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Text Alignment Options
                        Text(
                            text = "Text Alignment (टेक्स्ट अलाइनमेंट)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PostTextAlignment.values().forEach { align ->
                                val isSelected = textAlignment == align
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { textAlignment = align },
                                    label = { Text(align.label, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (align) {
                                                PostTextAlignment.CENTER -> Icons.Default.FormatAlignCenter
                                                PostTextAlignment.LEFT -> Icons.Default.FormatAlignLeft
                                                PostTextAlignment.RIGHT -> Icons.Default.FormatAlignRight
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicEmerald,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Text Color (फ़ॉन्ट रंग)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val colorOptions = listOf(
                                "White" to Color.White,
                                "Gold" to Color(0xFFFFD700),
                                "Cream" to Color(0xFFFDF5E6),
                                "Emerald" to Color(0xFF50C878),
                                "Cyan" to Color(0xFF00FFFF),
                                "Yellow" to Color(0xFFFFFF00)
                            )
                            colorOptions.forEach { (label, col) ->
                                FilterChip(
                                    selected = customTextColor == col,
                                    onClick = { customTextColor = col },
                                    label = { Text(label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicEmerald,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // TAB 3: ORNAMENTS & FRAMES
                    if (activeStudioTab == 3) {
                        Text(
                            text = "🎨 Ornaments & Islamic Accents (सजावट ऑप्शन)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show ﷽ (Bismillah Header)", fontSize = 13.sp)
                            Switch(checked = showBismillah, onCheckedChange = { showBismillah = it })
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Ornate Double Gold Frame", fontSize = 13.sp)
                            Switch(checked = showFrame, onCheckedChange = { showFrame = it })
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Poet (शायर) Badge", fontSize = 13.sp)
                            Switch(checked = showPoetBadge, onCheckedChange = { showPoetBadge = it })
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Naat Sharif Watermark", fontSize = 13.sp)
                            Switch(checked = showWatermark, onCheckedChange = { showWatermark = it })
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Footer Credit & Socials (फुटर क्रेडिट)", fontSize = 13.sp)
                            Switch(checked = showFooterCredit, onCheckedChange = { showFooterCredit = it })
                        }

                        if (showFooterCredit) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = footerCreditText,
                                onValueChange = { footerCreditText = it },
                                label = { Text("Footer Credit / Handle (जैसे: /Azazmadkiya)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Social Icons (Facebook, Instagram, YouTube)", fontSize = 12.sp)
                                Switch(checked = showFooterSocialIcons, onCheckedChange = { showFooterSocialIcons = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Without Background (Transparent Option)", fontSize = 12.sp)
                                Switch(checked = footerWithoutBackground, onCheckedChange = { footerWithoutBackground = it })
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Footer Size Scale: ${(footerSizeScale * 100).toInt()}%", fontSize = 12.sp)
                            Slider(
                                value = footerSizeScale,
                                onValueChange = { footerSizeScale = it },
                                valueRange = 0.7f..1.5f
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Footer Vertical Position Offset: ${footerVerticalOffset.toInt()}px", fontSize = 12.sp)
                            Slider(
                                value = footerVerticalOffset,
                                onValueChange = { footerVerticalOffset = it },
                                valueRange = -100f..100f
                            )

                            }
                    }

                    // TAB 4: EDIT KALAM & LANGUAGE
                    if (activeStudioTab == 4) {
                        Text(
                            text = "✏️ Edit Lyrics & Language (कलाम चुनें)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Language Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("हिंदी (Hindi)", "ગુજરાતી (Gujarati)", "English").forEachIndexed { idx, langName ->
                                FilterChip(
                                    selected = selectedLanguageIndex == idx,
                                    onClick = { selectedLanguageIndex = idx },
                                    label = { Text(langName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IslamicEmerald,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text("Title (शीर्षक)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customPoet,
                            onValueChange = { customPoet = it },
                            label = { Text("Poet (शायर)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customLyrics,
                            onValueChange = { customLyrics = it },
                            label = { Text("Lyrics on Image (बोल)") },
                            minLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================= EXPORT ACTIONS (SAVE & SHARE TO INSTAGRAM) =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Primary: Share to Instagram & Social Media
                Button(
                    onClick = {
                        isExporting = true
                        coroutineScope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                PostImageGenerator.renderPostBitmap(
                                    context = context,
                                    title = customTitle,
                                    poet = if (showPoetBadge) customPoet else "",
                                    lyrics = customLyrics,
                                    aspectRatio = selectedAspectRatio,
                                    theme = selectedTheme,
                                    fontSizeSp = customFontSizeSp,
                                    autoFit = isAutoFit,
                                    textAlignment = textAlignment,
                                    customImageBitmap = customImageBitmap,
                                    imageDimAlpha = imageDimAlpha,
                                    imageFitMode = imageFitMode,
                                    showBismillah = showBismillah,
                                    showFrame = showFrame,
                                    showPoetBadge = showPoetBadge,
                                    showWatermark = showWatermark,
                                    showFooterCredit = showFooterCredit,
                                    footerCreditText = footerCreditText,
                                    showFooterSocialIcons = showFooterSocialIcons,
                                    footerWithoutBackground = footerWithoutBackground,
                                    footerSizeScale = footerSizeScale,
                                    footerVerticalOffset = footerVerticalOffset,
                                     customTextColor = android.graphics.Color.argb((customTextColor.alpha * 255).toInt(), (customTextColor.red * 255).toInt(), (customTextColor.green * 255).toInt(), (customTextColor.blue * 255).toInt())
                                )
                            }
                            PostImageGenerator.sharePost(
                                context = context,
                                bitmap = bitmap,
                                caption = "✨ $customTitle • ${if (customPoet.isNotBlank()) "शायर: $customPoet" else "नाअत शरीफ"}\n\n$customLyrics\n\n#NaatSharif #Kalam #InstagramPost #IslamicQuotes #Shayar"
                            )
                            isExporting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("post_creator_share_button"),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Rendering HD Image...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = IslamicGoldBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📤 Share to Instagram / Social Media",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Secondary: Save to Device Gallery
                Button(
                    onClick = {
                        isExporting = true
                        coroutineScope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                PostImageGenerator.renderPostBitmap(
                                    context = context,
                                    title = customTitle,
                                    poet = if (showPoetBadge) customPoet else "",
                                    lyrics = customLyrics,
                                    aspectRatio = selectedAspectRatio,
                                    theme = selectedTheme,
                                    fontSizeSp = customFontSizeSp,
                                    autoFit = isAutoFit,
                                    textAlignment = textAlignment,
                                    customImageBitmap = customImageBitmap,
                                    imageDimAlpha = imageDimAlpha,
                                    imageFitMode = imageFitMode,
                                    showBismillah = showBismillah,
                                    showFrame = showFrame,
                                    showPoetBadge = showPoetBadge,
                                    showWatermark = showWatermark,
                                    showFooterCredit = showFooterCredit,
                                    footerCreditText = footerCreditText,
                                    showFooterSocialIcons = showFooterSocialIcons,
                                    footerWithoutBackground = footerWithoutBackground,
                                    footerSizeScale = footerSizeScale,
                                    footerVerticalOffset = footerVerticalOffset,
                                     customTextColor = android.graphics.Color.argb((customTextColor.alpha * 255).toInt(), (customTextColor.red * 255).toInt(), (customTextColor.green * 255).toInt(), (customTextColor.blue * 255).toInt())
                                )
                            }
                            val result = PostImageGenerator.saveBitmapToGallery(context, bitmap, customTitle)
                            result.onSuccess { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }.onFailure { err ->
                                Toast.makeText(context, "Error saving: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                            isExporting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("post_creator_save_button"),
                    enabled = !isExporting
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = IslamicGoldBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💾 Save Image to Gallery",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
