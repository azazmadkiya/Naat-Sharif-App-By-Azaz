package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NaatViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Start background sync service to periodically check Firebase and update local Room database
        com.example.data.sync.NaatBackgroundSyncService.start(this)
        com.example.data.sync.NaatBackgroundSyncService.schedulePeriodicAlarm(this)

        setContent {
            val viewModel: NaatViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            Screen.SPLASH -> SplashScreen(
                                onTimeout = {
                                    viewModel.checkAutoLoginAndNavigate()
                                }
                            )
                            Screen.AUTH -> AuthScreen(
                                onContinueAsGuest = { viewModel.continueAsGuest() },
                                onSignIn = { email, name, password, isSignUp ->
                                    viewModel.loginWithEmail(email, name, password, isSignUp)
                                },
                                onGoogleSignIn = { idToken -> viewModel.signInWithGoogleCredential(idToken) }
                            )
                            Screen.HOME -> HomeScreen(viewModel = viewModel)
                            Screen.READER -> ReaderScreen(viewModel = viewModel)
                            Screen.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            Screen.ADMIN_PORTAL -> AdminPortalScreen(viewModel = viewModel)
                            Screen.POST_CREATOR -> com.example.ui.screens.PostCreatorScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
