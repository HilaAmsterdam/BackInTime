package com.example.backintime.Repository

import com.example.backintime.Model.Dao.TimeCapsuleDao
import com.example.backintime.Model.Dao.TimeCapsuleEntity

class TimeCapsuleRepository(private val dao: TimeCapsuleDao) {

    suspend fun insertTimeCapsule(timeCapsule: TimeCapsuleEntity) {
        dao.insertTimeCapsule(timeCapsule)
    }

    suspend fun getAllTimeCapsules(): List<TimeCapsuleEntity> {
        return dao.getAllTimeCapsules()
    }

    suspend fun deleteTimeCapsule(timeCapsule: TimeCapsuleEntity) {
        // במקום להעביר localId, נעביר את ה-firebaseId כמפתח למחיקה
        dao.deleteTimeCapsule(timeCapsule.firebaseId)
    }

    suspend fun getTimeCapsulesByCreator(creatorId: String): List<TimeCapsuleEntity> {
        return dao.getTimeCapsulesByCreator(creatorId)
    }
}
