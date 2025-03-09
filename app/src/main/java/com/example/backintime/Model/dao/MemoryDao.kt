package com.example.backintime.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Query("SELECT * FROM memories ORDER BY openDate DESC")
    suspend fun getAllMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE creatorId = :userId ORDER BY openDate DESC")
    suspend fun getUserMemories(userId: String): List<MemoryEntity>

    @Query("DELETE FROM memories WHERE id = :memoryId")
    suspend fun deleteMemory(memoryId: Int)
}