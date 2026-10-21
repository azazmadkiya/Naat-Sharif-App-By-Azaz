package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NaatEntity::class,
        FavoriteNaatEntity::class,
        NaatLyricsEntity::class,
        OfflineNaatEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun naatDao(): NaatDao
    abstract fun favoriteNaatDao(): FavoriteNaatDao
    abstract fun naatLyricsDao(): NaatLyricsDao
    abstract fun offlineNaatDao(): OfflineNaatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "naat_sharif_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
