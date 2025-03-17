package com.example.backintime.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.backintime.Model.AppLocalDb
import com.example.backintime.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val localDb = AppLocalDb.getDatabase(applicationContext)
            val firestore = FirebaseFirestore.getInstance()
            val capsules = localDb.timeCapsuleDao().getAllTimeCapsules()
            Log.d("NotificationWorker", "Worker started, found ${capsules.size} capsules")

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return@withContext Result.success()
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfToday = calendar.timeInMillis

            val capsulesToNotify = capsules.filter { capsule ->
                (capsule.openDate in startOfToday until endOfToday) &&
                        !capsule.notified &&
                        capsule.creatorId == currentUser.uid
            }

            Log.d("NotificationWorker", "Capsules to notify: ${capsulesToNotify.size}")

            capsulesToNotify.forEach { capsule ->
                sendNotification(capsule.title, capsule.content)
                // Update Room: mark as notified.
                val updatedCapsule = capsule.copy(notified = true)
                localDb.timeCapsuleDao().insertTimeCapsule(updatedCapsule)
                firestore.collection("time_capsules")
                    .document(capsule.firebaseId)
                    .update("notified", true)
                    .addOnSuccessListener {
                        Log.d("NotificationWorker", "Firestore updated notified for capsule ${capsule.firebaseId}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationWorker", "Failed to update Firestore for capsule ${capsule.firebaseId}", e)
                    }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error in doWork", e)
            Result.failure()
        }
    }

    private fun sendNotification(title: String, content: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "time_capsule_channel"
        val channelName = "Time Capsule Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Time Capsule Opened: $title")
            .setContentText(content)
            .setSmallIcon(R.drawable.back_in_time_circle_logo)
            .build()
        notificationManager.notify(title.hashCode(), notification)
    }
}
