package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.ui.components.DonationDialog
import com.example.ui.components.PolicyDialog
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldBright
import com.example.ui.viewmodel.NaatViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.NotificationHelper

@Composable
fun SettingsScreen(viewModel: NaatViewModel) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontSizeSp by viewModel.fontSizeSp.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showDonationDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDataSafetyDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.isPermissionGranted(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationHelper.sendNotification(context)
            Toast.makeText(context, "Notification permission granted! Sent sample reminder.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission was not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler {
        viewModel.navigateBack()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Profile Card (Matching Screenshot)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE4F0E8)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Mint Container with Deep Emerald Open Book Icon (Matching Reference Design)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFE2EFE7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_category_book),
                            contentDescription = "Naat Sharif Logo",
                            tint = Color(0xFF135B37),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (currentUser.isGuest) "Guest User" else currentUser.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentUser.isGuest) "Viewer / Guest Mode" else "${currentUser.role.displayName} • ${currentUser.email}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Donation Banner Card (Deep Emerald with white button)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Naat Sharif / Kalam Sharif Hindi - Gujarati",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showDonationDialog = true },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = IslamicEmerald
                        ),
                        modifier = Modifier.testTag("settings_donation_button")
                    ) {
                        Text(
                            text = "डोनेशन करे",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reading Text Size Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE2EFE7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = Color(0xFF135B37),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Reading Text Size ($fontSizeSp sp)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Adjust font size for reading lyrics comfortably",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = fontSizeSp.toFloat(),
                        onValueChange = { viewModel.updateFontSize(it.toInt()) },
                        valueRange = 14f..32f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = IslamicEmerald,
                            activeTrackColor = IslamicEmerald
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("font_size_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Theme Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDarkMode) Icons.Default.NightlightRound else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (isDarkMode) IslamicGoldBright else Color(0xFFD48B00),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "App Theme / ऐप थीम",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkMode) "🌙 Dark Mode On / डार्क मोड चालू" else "☀️ Light Mode On / लाइट मोड चालू",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IslamicGoldBright,
                                checkedTrackColor = IslamicEmerald
                            ),
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick light / dark buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Light mode pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (!isDarkMode) IslamicEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.toggleDarkMode(false) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (!isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Light / लाइट",
                                    color = if (!isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Dark mode pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isDarkMode) IslamicEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.toggleDarkMode(true) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NightlightRound,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dark / डार्क",
                                    color = if (isDarkMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Notification Permission Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (hasNotificationPermission) IslamicEmerald.copy(alpha = 0.15f) else Color(0xFFFFECE0),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (hasNotificationPermission) IslamicEmerald else Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Notification Permission",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (hasNotificationPermission) "Allowed • Daily Durood alerts active" else "Permission Required • Tap to allow",
                                    fontSize = 12.sp,
                                    color = if (hasNotificationPermission) IslamicEmerald else Color(0xFFD84315)
                                )
                            }
                        }

                        Switch(
                            checked = hasNotificationPermission,
                            onCheckedChange = { shouldEnable ->
                                if (shouldEnable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        hasNotificationPermission = true
                                        NotificationHelper.sendNotification(context)
                                    }
                                } else {
                                    Toast.makeText(context, "To turn off notifications, please manage them in Android App Settings.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IslamicGoldBright,
                                checkedTrackColor = IslamicEmerald
                            ),
                            modifier = Modifier.testTag("notification_permission_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Receive daily blessed Durood Sharif reminders, Jummah Mubarak alerts, and newly published Kalam Sharif notifications.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val sent = NotificationHelper.sendNotification(context)
                                    if (sent) {
                                        Toast.makeText(context, "Test notification sent successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please grant notification permission first.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasNotificationPermission) IslamicEmerald else Color(0xFFE65100)
                            ),
                            modifier = Modifier.testTag("send_test_notification_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasNotificationPermission) "Send Test Alert" else "Grant Permission",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Items (Share, Rate)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Share App
                    SettingsActionItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Share with friends & family",
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Download Naat Sharif - Islamic Kalam in Hindi, Gujarati & English:\nhttps://play.google.com/store/apps/details?id=com.naatsharif.app.azaz"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Naat Sharif App"))
                        }
                    )

                    // Rate Us
                    SettingsActionItem(
                        icon = Icons.Default.Star,
                        title = "Rate Us",
                        subtitle = "Give 5 stars on Play Store",
                        onClick = {
                            try {
                                val uri = Uri.parse("market://details?id=com.naatsharif.app.azaz")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Thank you for supporting Naat Sharif app!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legal & Policies Header
            Text(
                text = "Legal, Policies & Compliance",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicEmerald,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Legal Cards
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Privacy Policy
                    SettingsActionItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy / प्रायवेसी पॉलिसी",
                        subtitle = "User data, Firebase storage & zero tracking",
                        onClick = { showPrivacyDialog = true }
                    )

                    // Terms & Conditions
                    SettingsActionItem(
                        icon = Icons.Default.Description,
                        title = "Terms & Conditions / नियम व शर्ते",
                        subtitle = "Spiritual usage, poetry rights & moderation",
                        onClick = { showTermsDialog = true }
                    )

                    // Data Safety & Account Deletion
                    SettingsActionItem(
                        icon = Icons.Default.Delete,
                        title = "Data Safety & Account Deletion",
                        subtitle = "Google Play compliance & permanent deletion request",
                        onClick = { showDataSafetyDialog = true }
                    )

                    // Login / Switch Account
                    SettingsActionItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = if (currentUser.isGuest) "Sign In / Create Account" else "Logout / Switch Account",
                        subtitle = if (currentUser.isGuest) "Sign in to unlock admin or contributor access" else "Signed in as ${currentUser.email}",
                        onClick = {
                            if (currentUser.isGuest) {
                                viewModel.navigateTo(Screen.AUTH)
                            } else {
                                showLogoutConfirm = true
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Card: "Developed By Azazmadkiya"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE4F0E8),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Developed By ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Azazmadkiya",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = IslamicEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Dialogs
        if (showDonationDialog) {
            DonationDialog(onDismiss = { showDonationDialog = false })
        }

        if (showPrivacyDialog) {
            PolicyDialog(
                title = "Privacy Policy / प्रायवेसी पॉलिसी",
                content = """
Naat Sharif App Privacy Policy:

1. User Privacy & Zero Tracking:
We value your spiritual journey. We do not sell, rent, or monetize your personal data. 

2. Account Data:
If you register or sign in, we only store your email address and profile name to manage contributor/admin permissions. Guest mode requires zero personal info.

3. Offline Room Storage:
Your favorites, bookmarks, and font size preferences are stored entirely locally on your device via Room Database.

4. Server & Cloud Backend:
Any data synchronization uses secure Firebase backend services with zero tracking.

Contact Developer: azazmadkiya@gmail.com
                """.trimIndent(),
                onDismiss = { showPrivacyDialog = false }
            )
        }

        if (showTermsDialog) {
            PolicyDialog(
                title = "Terms & Conditions / नियम व शर्ते",
                content = """
Terms and Spiritual Usage Guidelines:

1. Holy Kalam & Naat Sharif:
All poetry, naats, and hamd included in this application are preserved for personal religious reading, recitation, and spiritual benefit.

2. Content Moderation:
All submissions by contributors undergo strict review to ensure reverence, linguistic accuracy across Hindi, Gujarati, and Roman Urdu.

3. Copyright & Intellectual Property:
Writers and poets are credited where known. If you represent an author and have queries, contact azazmadkiya@gmail.com.
                """.trimIndent(),
                onDismiss = { showTermsDialog = false }
            )
        }

        if (showDataSafetyDialog) {
            AlertDialog(
                onDismissRequest = { showDataSafetyDialog = false },
                title = { Text("Account & Data Deletion") },
                text = {
                    Text(
                        "In compliance with Google Play Data Safety policies, you can request complete deletion of your account and any synced data.\n\nTo request instant deletion, tap Request Deletion below or email azazmadkiya@gmail.com."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.logout()
                            showDataSafetyDialog = false
                            Toast.makeText(context, "Account data wiped from device", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Delete My Data")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDataSafetyDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to sign out and return to the login screen?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutConfirm = false
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE2EFE7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF135B37),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp)
        )
    }
}
