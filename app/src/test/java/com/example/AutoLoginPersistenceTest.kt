package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import com.example.ui.viewmodel.NaatViewModel
import com.example.ui.viewmodel.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AutoLoginPersistenceTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext<Application>()
        // Clear shared preferences
        val prefs = app.getSharedPreferences("naat_sharif_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @Test
    fun testUserRemainsLoggedInAfterAppRestart() {
        // Step 1: User logs in
        val vm1 = NaatViewModel(app)
        vm1.login("azazmadkiya@gmail.com", "Azaz Madkiya", "uid_12345")

        assertEquals(Screen.HOME, vm1.currentScreen.value)
        assertFalse(vm1.currentUser.value.isGuest)
        assertEquals("azazmadkiya@gmail.com", vm1.currentUser.value.email)
        assertEquals(UserRole.SUPER_ADMIN, vm1.currentUser.value.role)

        // Step 2: Simulate app being closed and re-opened (new ViewModel instance)
        val vm2 = NaatViewModel(app)
        // Check initial state before splash finishes
        assertFalse("User should not be guest in restored session", vm2.currentUser.value.isGuest)
        assertEquals("azazmadkiya@gmail.com", vm2.currentUser.value.email)
        assertEquals(UserRole.SUPER_ADMIN, vm2.currentUser.value.role)

        // Step 3: Splash timeout triggers auto-login check
        vm2.checkAutoLoginAndNavigate()

        // User should be immediately routed to HOME, NOT AUTH!
        assertEquals("Reopened app should navigate to HOME, keeping user logged in", Screen.HOME, vm2.currentScreen.value)
    }

    @Test
    fun testUnauthenticatedUserRoutesToAuthOnRestart() {
        // Fresh start without login
        val vm = NaatViewModel(app)
        assertTrue(vm.currentUser.value.isGuest)

        vm.checkAutoLoginAndNavigate()
        assertEquals(Screen.AUTH, vm.currentScreen.value)
    }

    @Test
    fun testExplicitLogoutReturnsToAuthOnRestart() {
        val vm1 = NaatViewModel(app)
        vm1.login("reader@example.com", "Reader User")
        vm1.logout()

        assertEquals(Screen.AUTH, vm1.currentScreen.value)

        // Reopen app after logout
        val vm2 = NaatViewModel(app)
        vm2.checkAutoLoginAndNavigate()
        assertEquals(Screen.AUTH, vm2.currentScreen.value)
    }
}
