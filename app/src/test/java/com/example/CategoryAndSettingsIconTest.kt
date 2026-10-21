package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CategoryAndSettingsIconTest {

    @Test
    fun testAppLogoDrawableExistsAndLoads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = context.getDrawable(R.drawable.app_logo)
        assertNotNull("R.drawable.app_logo must exist and load", drawable)
        assertTrue("Drawable width must be > 0", (drawable?.intrinsicWidth ?: 0) > 0)
    }

    @Test
    fun testAppIconDrawableExistsAndLoads() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = context.getDrawable(R.drawable.ic_app_icon)
        assertNotNull("R.drawable.ic_app_icon must exist and load", drawable)
    }
}
