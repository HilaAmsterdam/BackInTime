package com.example.backintime.Model.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.backintime.Model.Dao.TimeCapsuleEntity

@Dao
interface TimeCapsuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeCapsule(timeCapsule: TimeCapsuleEntity)

    @Query("SELECT * FROM time_capsules ORDER BY openDate DESC")
    suspend fun getAllTimeCapsules(): List<TimeCapsuleEntity>

    @Query("DELETE FROM time_capsules WHERE localId = :localId")
    suspend fun deleteTimeCapsule(localId: Int)
}
