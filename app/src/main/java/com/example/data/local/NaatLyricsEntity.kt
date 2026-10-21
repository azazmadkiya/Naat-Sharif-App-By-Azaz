package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity to store Naat lyrics, authors, and language tags locally for offline access.
 */
@Entity(tableName = "naat_lyrics")
data class NaatLyricsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String, // Author or poet of the Naat
    val lyrics: String, // Full text of the Naat lyrics
    val languageTags: String, // Comma-separated language tags (e.g., "hi,gu,en", "hi", "ur", "en")
    val titleHindi: String = title,
    val titleGujarati: String = "",
    val titleEnglish: String = "",
    val lyricsHindi: String = lyrics,
    val lyricsGujarati: String = "",
    val lyricsEnglish: String = "",
    val category: String = "Naat Sharif",
    val addedBy: String = "Azazmadkiya",
    val isAvailableOffline: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val authors: String
        get() = author

    val languageTagList: List<String>
        get() = languageTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

fun NaatEntity.toNaatLyricsEntity(): NaatLyricsEntity = NaatLyricsEntity(
    id = id,
    title = titleHindi.ifBlank { titleEnglish }.ifBlank { titleGujarati },
    author = poet.ifBlank { "Unknown" },
    lyrics = lyricsHindi.ifBlank { lyricsEnglish }.ifBlank { lyricsGujarati },
    languageTags = "hi,gu,en",
    titleHindi = titleHindi,
    titleGujarati = titleGujarati,
    titleEnglish = titleEnglish,
    lyricsHindi = lyricsHindi,
    lyricsGujarati = lyricsGujarati,
    lyricsEnglish = lyricsEnglish,
    category = categoryName,
    addedBy = displayAddedBy
)
