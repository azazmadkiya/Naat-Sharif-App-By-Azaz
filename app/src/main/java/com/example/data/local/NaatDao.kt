package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NaatDao {
    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 1 ORDER BY orderIndex ASC, titleHindi ASC")
    fun getAllApproved(): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 1 AND (categoryId = :categoryId OR categoryName = :categoryId) ORDER BY orderIndex ASC, titleHindi ASC")
    fun getByCategory(categoryId: String): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 0 ORDER BY createdAt DESC")
    fun getPendingApprovals(): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 1 ORDER BY createdAt DESC")
    fun getTrash(): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE submittedBy = :email ORDER BY createdAt DESC")
    fun getBySubmitter(email: String): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 1 ORDER BY orderIndex ASC, titleHindi ASC")
    suspend fun getAllApprovedList(): List<NaatEntity>

    @Query("SELECT * FROM naats WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<NaatEntity?>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 1 AND (author LIKE '%' || :author || '%' OR poet LIKE '%' || :author || '%') ORDER BY titleHindi ASC")
    fun getByAuthor(author: String): Flow<List<NaatEntity>>

    @Query("SELECT * FROM naats WHERE isDeleted = 0 AND isApproved = 1 AND languageTags LIKE '%' || :tag || '%' ORDER BY titleHindi ASC")
    fun getByLanguageTag(tag: String): Flow<List<NaatEntity>>

    @Query("""
        SELECT * FROM naats 
        WHERE isDeleted = 0 AND isApproved = 1 AND (
            titleHindi LIKE '%' || :query || '%' OR 
            titleGujarati LIKE '%' || :query || '%' OR 
            titleEnglish LIKE '%' || :query || '%' OR 
            poet LIKE '%' || :query || '%' OR 
            lyricsHindi LIKE '%' || :query || '%' OR 
            lyricsGujarati LIKE '%' || :query || '%' OR 
            lyricsEnglish LIKE '%' || :query || '%'
        )
        ORDER BY titleHindi ASC
    """)
    fun searchNaats(query: String): Flow<List<NaatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(naat: NaatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(naats: List<NaatEntity>)

    @Update
    suspend fun update(naat: NaatEntity)

    @Query("UPDATE naats SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE naats SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE naats SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM naats WHERE id = :id")
    suspend fun deletePermanently(id: String)

    @Query("UPDATE naats SET isApproved = 1 WHERE id = :id")
    suspend fun approve(id: String)

    @Query("SELECT COUNT(*) FROM naats WHERE isDeleted = 0 AND isApproved = 1")
    suspend fun countApproved(): Int

    @Query("SELECT COUNT(*) FROM naats WHERE isDeleted = 0 AND isApproved = 0")
    suspend fun countPending(): Int

    @Query("SELECT COUNT(*) FROM naats WHERE isDeleted = 1")
    suspend fun countTrash(): Int

    @Query("SELECT COUNT(*) FROM naats WHERE isDeleted = 0 AND isApproved = 1 AND (categoryId = :catId OR categoryName = :catId)")
    suspend fun countByCategory(catId: String): Int
}
