package com.example.backintime.Model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.backintime.Model.Dao.TimeCapsuleDao
import com.example.backintime.Model.Dao.UserDao
import com.example.backintime.Model.Dao.TimeCapsuleEntity

@Database(
    entities = [
        TimeCapsuleEntity::class,
        com.example.backintime.Model.User::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppLocalDb : RoomDatabase() {

    abstract fun timeCapsuleDao(): TimeCapsuleDao
    abstract fun userDao(): UserDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
