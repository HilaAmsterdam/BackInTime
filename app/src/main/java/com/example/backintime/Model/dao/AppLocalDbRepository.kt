package com.example.backintime.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.backintime.model.dao.MemoryDao
import com.example.backintime.model.dao.MemoryEntity

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppLocalDbRepository? = null

        fun getDatabase(context: Context): AppLocalDbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDbRepository::class.java,
                    "back_in_time_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
