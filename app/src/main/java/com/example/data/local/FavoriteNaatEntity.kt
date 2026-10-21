package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity to store user-favorited Naats locally.
 * This enables offline caching of favorite Naat Sharif lyrics and instant retrieval.
 */
@Entity(tableName = "favorite_naats")
data class FavoriteNaatEntity(
    @PrimaryKey
    val naatId: String,
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
    val submittedBy: String = "Azazmadkiya",
    val addedByName: String = "",
    val favoritedAt: Long = System.currentTimeMillis()
)

fun FavoriteNaatEntity.toNaatEntity(): NaatEntity {
    return NaatEntity(
        id = naatId,
        titleHindi = titleHindi,
        titleGujarati = titleGujarati,
        titleEnglish = titleEnglish,
        categoryId = categoryId,
        categoryName = categoryName,
        poet = poet,
        publisher = publisher,
        lyricsHindi = lyricsHindi,
        lyricsGujarati = lyricsGujarati,
        lyricsEnglish = lyricsEnglish,
        audioUrl = audioUrl,
        isFavorite = true,
        isApproved = true,
        isDeleted = false,
        submittedBy = submittedBy,
        addedByName = addedByName,
        createdAt = favoritedAt
    )
}

fun NaatEntity.toFavoriteEntity(timestamp: Long = System.currentTimeMillis()): FavoriteNaatEntity {
    return FavoriteNaatEntity(
        naatId = id,
        titleHindi = titleHindi,
        titleGujarati = titleGujarati,
        titleEnglish = titleEnglish,
        categoryId = categoryId,
        categoryName = categoryName,
        poet = poet,
        publisher = publisher,
        lyricsHindi = lyricsHindi,
        lyricsGujarati = lyricsGujarati,
        lyricsEnglish = lyricsEnglish,
        audioUrl = audioUrl,
        submittedBy = submittedBy,
        addedByName = addedByName,
        favoritedAt = timestamp
    )
}
