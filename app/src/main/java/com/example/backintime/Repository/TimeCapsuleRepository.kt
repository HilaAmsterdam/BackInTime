package com.example.backintime.Repository

import com.example.backintime.Model.Dao.TimeCapsuleDao
import com.example.backintime.Model.Dao.UserDao
import com.example.backintime.Model.Dao.TimeCapsuleEntity
import com.example.backintime.Model.User

class TimeCapsuleRepository(
    private val timeCapsuleDao: TimeCapsuleDao,
    private val userDao: UserDao
) {
    suspend fun insertTimeCapsule(timeCapsule: TimeCapsuleEntity) {
        timeCapsuleDao.insertTimeCapsule(timeCapsule)
    }

    suspend fun updateTimeCapsule(timeCapsule: TimeCapsuleEntity) {
        timeCapsuleDao.insertTimeCapsule(timeCapsule)
    }

    suspend fun getAllTimeCapsules(): List<TimeCapsuleEntity> {
        return timeCapsuleDao.getAllTimeCapsules()
    }

    suspend fun deleteTimeCapsule(timeCapsule: TimeCapsuleEntity) {
        timeCapsuleDao.deleteTimeCapsule(timeCapsule.firebaseId)
    }

    suspend fun getTimeCapsuleById(firebaseId: String): TimeCapsuleEntity? {
        return timeCapsuleDao.getMemoryByFirebaseId(firebaseId)
    }

    suspend fun getTimeCapsulesByCreator(creatorId: String): List<TimeCapsuleEntity> {
        return timeCapsuleDao.getTimeCapsulesByCreator(creatorId)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}
