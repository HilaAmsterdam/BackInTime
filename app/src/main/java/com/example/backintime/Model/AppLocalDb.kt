package com.example.backintime.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.backintime.Model.Dao.TimeCapsuleDao
import com.example.backintime.Model.Dao.TimeCapsuleEntity

@Database(entities = [TimeCapsuleEntity::class], version = 2, exportSchema = false)
abstract class AppLocalDb : RoomDatabase() {

    abstract fun timeCapsuleDao(): TimeCapsuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppLocalDb? = null

        fun getDatabase(context: Context): AppLocalDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDb::class.java,
                    "back_in_time_db"
                )
                    .fallbackToDestructiveMigration() // מאפשר בנייה מחדש של המסד במקרה של שינוי בסכמה
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
