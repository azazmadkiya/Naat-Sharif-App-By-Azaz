package com.example.data.model

enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    EDITOR("Editor"),
    CONTRIBUTOR("Contributor"),
    READER("Reader"),
    VIEWER("Viewer");

    val isAdmin: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN

    val isNonAdmin: Boolean
        get() = !isAdmin

    val canEdit: Boolean
        get() = this == SUPER_ADMIN || this == ADMIN || this == EDITOR

    companion object {
        fun fromString(value: String?): UserRole {
            if (value.isNullOrBlank()) return READER
            val cleaned = value.trim()
            return when {
                cleaned.equals("SUPER_ADMIN", ignoreCase = true) || cleaned.equals("SUPERADMIN", ignoreCase = true) -> SUPER_ADMIN
                cleaned.equals("ADMIN", ignoreCase = true) -> ADMIN
                cleaned.equals("EDITOR", ignoreCase = true) -> EDITOR
                cleaned.equals("CONTRIBUTOR", ignoreCase = true) -> CONTRIBUTOR
                cleaned.equals("READER", ignoreCase = true) -> READER
                cleaned.equals("VIEWER", ignoreCase = true) -> VIEWER
                cleaned.contains("SUPER", ignoreCase = true) -> SUPER_ADMIN
                cleaned.contains("ADMIN", ignoreCase = true) -> ADMIN
                cleaned.contains("EDIT", ignoreCase = true) -> EDITOR
                cleaned.contains("CONTRIB", ignoreCase = true) -> CONTRIBUTOR
                else -> READER
            }
        }
    }
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "Guest User",
    val role: UserRole = UserRole.READER,
    val isGuest: Boolean = true
) {
    val isAdmin: Boolean
        get() = role.isAdmin

    val isNonAdmin: Boolean
        get() = role.isNonAdmin

    val canAddNaat: Boolean
        get() = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN || role == UserRole.EDITOR || role == UserRole.CONTRIBUTOR

    val canEditNaat: Boolean
        get() = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN || role == UserRole.EDITOR

    val canDeleteNaat: Boolean
        get() = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN

    val canApproveNaat: Boolean
        get() = role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN

    val isSuperAdmin: Boolean
        get() = role == UserRole.SUPER_ADMIN

    val isContributor: Boolean
        get() = role == UserRole.CONTRIBUTOR

    val isReader: Boolean
        get() = role == UserRole.READER || role == UserRole.VIEWER
}

object RoleManager {
    val SUPER_ADMIN_EMAILS = setOf("azazmadkiya@gmail.com")
    val CONTRIBUTOR_EMAILS = setOf("info.azazmadkiya@gmail.com", "madkiyaahemad@gmail.com")

    fun getRoleForEmail(email: String): UserRole {
        val trimmed = email.trim().lowercase()
        return when {
            trimmed in SUPER_ADMIN_EMAILS -> UserRole.SUPER_ADMIN
            trimmed in CONTRIBUTOR_EMAILS -> UserRole.CONTRIBUTOR
            trimmed.contains("superadmin") -> UserRole.SUPER_ADMIN
            trimmed.contains("admin") -> UserRole.ADMIN
            trimmed.contains("editor") -> UserRole.EDITOR
            trimmed.contains("contributor") -> UserRole.CONTRIBUTOR
            else -> UserRole.READER
        }
    }
}
