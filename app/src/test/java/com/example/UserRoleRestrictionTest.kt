package com.example

import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRoleRestrictionTest {

    @Test
    fun testUserRoleParsing() {
        assertEquals(UserRole.READER, UserRole.fromString("Reader"))
        assertEquals(UserRole.READER, UserRole.fromString("reader"))
        assertEquals(UserRole.READER, UserRole.fromString("READER"))
        assertEquals(UserRole.EDITOR, UserRole.fromString("Editor"))
        assertEquals(UserRole.EDITOR, UserRole.fromString("editor"))
        assertEquals(UserRole.ADMIN, UserRole.fromString("Admin"))
        assertEquals(UserRole.SUPER_ADMIN, UserRole.fromString("SUPER_ADMIN"))
        assertEquals(UserRole.CONTRIBUTOR, UserRole.fromString("Contributor"))
    }

    @Test
    fun testReaderRoleRestrictions() {
        val readerProfile = UserProfile(
            uid = "reader_123",
            email = "reader@example.com",
            fullName = "Reader User",
            role = UserRole.READER,
            isGuest = false
        )

        assertTrue(readerProfile.isReader)
        assertTrue(readerProfile.isNonAdmin)
        assertFalse(readerProfile.isAdmin)
        assertFalse(readerProfile.isSuperAdmin)
        assertFalse(readerProfile.canEditNaat) // Restricted access to edit features for non-admins
        assertFalse(readerProfile.canDeleteNaat)
        assertFalse(readerProfile.canApproveNaat)
    }

    @Test
    fun testEditorRolePermissions() {
        val editorProfile = UserProfile(
            uid = "editor_123",
            email = "editor@example.com",
            fullName = "Editor User",
            role = UserRole.EDITOR,
            isGuest = false
        )

        assertFalse(editorProfile.isReader)
        assertTrue(editorProfile.canEditNaat) // Editors can edit
        assertTrue(editorProfile.canAddNaat)
        assertFalse(editorProfile.canDeleteNaat) // Only admin can delete
    }

    @Test
    fun testAdminRolePermissions() {
        val adminProfile = UserProfile(
            uid = "admin_123",
            email = "admin@example.com",
            fullName = "Admin User",
            role = UserRole.ADMIN,
            isGuest = false
        )

        assertTrue(adminProfile.isAdmin)
        assertFalse(adminProfile.isNonAdmin)
        assertTrue(adminProfile.canEditNaat)
        assertTrue(adminProfile.canDeleteNaat)
        assertTrue(adminProfile.canApproveNaat)
    }
}
