package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "naats")
data class NaatEntity(
    @PrimaryKey
    val id: String,
    val titleHindi: String,
    val titleGujarati: String,
    val titleEnglish: String,
    val categoryId: String,
    val categoryName: String,
    val poet: String,
    val publisher: String = "",
    val lyricsHindi: String,
    val lyricsGujarati: String,
    val lyricsEnglish: String,
    val audioUrl: String? = null,
    val isFavorite: Boolean = false,
    val isApproved: Boolean = true,
    val isDeleted: Boolean = false,
    val submittedBy: String = "Azazmadkiya",
    val addedByName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0,
    val author: String = poet,
    val languageTags: String = "hi,gu,en",
    val lyrics: String = lyricsHindi
) {
    val authors: String
        get() = author.ifBlank { poet }

    val displayAddedBy: String
        get() = when {
            addedByName.isNotBlank() -> addedByName
            submittedBy.isNotBlank() && submittedBy != "Admin" -> submittedBy.substringBefore("@")
            else -> "Azazmadkiya"
        }
}
