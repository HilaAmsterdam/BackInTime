package com.example.backintime.Model

import android.content.Context
import com.example.backintime.Model.AppLocalDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SyncManager {
    fun listenFirebaseDataToRoom(context: Context) {
        val firebaseModel = FirebaseModel()
        val localDb = AppLocalDb.getDatabase(context)
        firebaseModel.listenTimeCapsulesFromFirebase { capsules ->
            CoroutineScope(Dispatchers.IO).launch {
                localDb.timeCapsuleDao().clearTimeCapsules()
                capsules.forEach { capsule ->
                    localDb.timeCapsuleDao().insertTimeCapsule(capsule)
                }
            }
        }
    }
}
