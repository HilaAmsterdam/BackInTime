package com.example.backintime.Model

import android.content.Context
import com.example.backintime.Model.AppLocalDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SyncManager {
    fun syncFirebaseDataToRoom(context: Context) {
        val firebaseModel = FirebaseModel()
        val localDb = AppLocalDb.getDatabase(context)
        firebaseModel.fetchTimeCapsulesFromFirebase { capsules ->
            CoroutineScope(Dispatchers.IO).launch {
                capsules.forEach { capsule ->
                    localDb.timeCapsuleDao().insertTimeCapsule(capsule)
                }
            }
        }
    }
}
