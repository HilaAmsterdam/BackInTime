package com.example.backintime.repository


import com.example.backintime.model.dao.MemoryDao
import com.example.backintime.model.dao.MemoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MemoryRepository(private val dao: MemoryDao) {
    private val db = FirebaseFirestore.getInstance()

    suspend fun insertMemory(memory: MemoryEntity) {

        dao.insertMemory(memory)

        val memoryMap = mapOf(
            "title" to memory.title,
            "content" to memory.content,
            "openDate" to memory.openDate,
            "imageUrl" to memory.imageUrl,
            "creatorId" to memory.creatorId,
            "creatorEmail" to memory.creatorEmail
        )

        db.collection("memories").document(memory.id.toString()).set(memoryMap).await()
    }

    suspend fun getAllMemories(): List<MemoryEntity> = dao.getAllMemories()

    suspend fun getUserMemories(userId: String): List<MemoryEntity> = dao.getUserMemories(userId)

    suspend fun deleteMemory(memoryId: Int) {
        dao.deleteMemory(memoryId)
        db.collection("memories").document(memoryId.toString()).delete().await()
    }
}
