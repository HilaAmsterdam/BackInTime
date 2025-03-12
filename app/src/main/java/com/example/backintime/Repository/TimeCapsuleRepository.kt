package com.example.backintime.Repository


import com.example.backintime.Model.Dao.TimeCapsuleDao
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TimeCapsuleRepository(private val dao: TimeCapsuleDao) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun insertCapsule(entity: TimeCapsuleEntity) {

        dao.insertTimeCapsule(entity)


        val dataMap = mapOf(
            "title" to entity.title,
            "content" to entity.content,
            "openDate" to entity.openDate,
            "imageUrl" to entity.imageUrl,
            "creatorName" to entity.creatorName,
            "creatorId" to entity.creatorId
        )

        firestore.collection("time_capsules")
            .document(entity.firebaseId.ifEmpty { System.currentTimeMillis().toString() })
            .set(dataMap)
            .await()
    }

    suspend fun getAllLocalCapsules(): List<TimeCapsuleEntity> {
        return dao.getAllTimeCapsules()
    }

    suspend fun deleteCapsuleByLocalId(localId: Int) {
        dao.deleteTimeCapsule(localId)

    }
}
