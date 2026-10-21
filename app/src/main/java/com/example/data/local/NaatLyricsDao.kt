package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local offline access to Naat lyrics, authors, and language tags.
 */
@Dao
interface NaatLyricsDao {
    @Query("SELECT * FROM naat_lyrics ORDER BY title ASC")
    fun getAllOfflineLyrics(): Flow<List<NaatLyricsEntity>>

    @Query("SELECT * FROM naat_lyrics ORDER BY title ASC")
    suspend fun getAllOfflineLyricsList(): List<NaatLyricsEntity>

    @Query("SELECT * FROM naat_lyrics WHERE id = :id LIMIT 1")
    fun getLyricsById(id: String): Flow<NaatLyricsEntity?>

    @Query("SELECT * FROM naat_lyrics WHERE id = :id LIMIT 1")
    suspend fun getLyricsByIdSuspend(id: String): NaatLyricsEntity?

    @Query("SELECT * FROM naat_lyrics WHERE author = :author OR author LIKE '%' || :author || '%' ORDER BY title ASC")
    fun getLyricsByAuthor(author: String): Flow<List<NaatLyricsEntity>>

    @Query("SELECT * FROM naat_lyrics WHERE languageTags LIKE '%' || :tag || '%' ORDER BY title ASC")
    fun getLyricsByLanguageTag(tag: String): Flow<List<NaatLyricsEntity>>

    @Query("""
        SELECT * FROM naat_lyrics 
        WHERE title LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%' 
           OR lyrics LIKE '%' || :query || '%' 
           OR languageTags LIKE '%' || :query || '%'
        ORDER BY title ASC
    """)
    fun searchLyrics(query: String): Flow<List<NaatLyricsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NaatLyricsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<NaatLyricsEntity>)

    @Update
    suspend fun update(entity: NaatLyricsEntity)

    @Delete
    suspend fun delete(entity: NaatLyricsEntity)

    @Query("DELETE FROM naat_lyrics WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM naat_lyrics")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM naat_lyrics")
    suspend fun count(): Int
}
