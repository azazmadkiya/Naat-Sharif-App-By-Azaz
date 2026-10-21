package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room database entity storing Naat lyrics, authors, and language tags locally for offline access.
 */
@Entity(tableName = "offline_naats")
data class OfflineNaatEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String, // Author / Poet
    val lyrics: String, // Lyrics text
    val languageTags: String, // Language tags e.g. "hi,gu,en"
    val isAvailableOffline: Boolean = true,
    val cachedAt: Long = System.currentTimeMillis()
) {
    val authors: String
        get() = author
}

fun NaatEntity.toOfflineNaatEntity(): OfflineNaatEntity = OfflineNaatEntity(
    id = id,
    title = titleHindi.ifBlank { titleEnglish }.ifBlank { titleGujarati },
    author = poet.ifBlank { "Unknown" },
    lyrics = lyricsHindi.ifBlank { lyricsEnglish }.ifBlank { lyricsGujarati },
    languageTags = "hi,gu,en"
)

@Dao
interface OfflineNaatDao {
    @Query("SELECT * FROM offline_naats ORDER BY title ASC")
    fun getAllOffline(): Flow<List<OfflineNaatEntity>>

    @Query("SELECT * FROM offline_naats ORDER BY title ASC")
    suspend fun getAllOfflineList(): List<OfflineNaatEntity>

    @Query("SELECT * FROM offline_naats WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<OfflineNaatEntity?>

    @Query("SELECT * FROM offline_naats WHERE author LIKE '%' || :author || '%'")
    fun getByAuthor(author: String): Flow<List<OfflineNaatEntity>>

    @Query("SELECT * FROM offline_naats WHERE languageTags LIKE '%' || :tag || '%'")
    fun getByLanguageTag(tag: String): Flow<List<OfflineNaatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(naat: OfflineNaatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(naats: List<OfflineNaatEntity>)

    @Update
    suspend fun update(naat: OfflineNaatEntity)

    @Delete
    suspend fun delete(naat: OfflineNaatEntity)

    @Query("DELETE FROM offline_naats WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM offline_naats")
    suspend fun count(): Int
}
