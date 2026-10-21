package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local user-favorited Naats in Room.
 * Provides reactive Flow observations and suspend mutation functions.
 */
@Dao
interface FavoriteNaatDao {
    @Query("SELECT * FROM favorite_naats ORDER BY favoritedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteNaatEntity>>

    @Query("SELECT * FROM favorite_naats WHERE naatId = :naatId LIMIT 1")
    fun getFavoriteById(naatId: String): Flow<FavoriteNaatEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_naats WHERE naatId = :naatId)")
    fun isFavorite(naatId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_naats WHERE naatId = :naatId)")
    suspend fun isFavoriteSync(naatId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteNaatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavorites(favorites: List<FavoriteNaatEntity>)

    @Query("DELETE FROM favorite_naats WHERE naatId = :naatId")
    suspend fun deleteFavoriteById(naatId: String)

    @Query("SELECT COUNT(*) FROM favorite_naats")
    suspend fun countFavorites(): Int

    @Query("DELETE FROM favorite_naats")
    suspend fun clearAllFavorites()
}
