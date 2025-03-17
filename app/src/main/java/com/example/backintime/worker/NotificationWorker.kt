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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppLocalDb.getDatabase(applicationContext)
                val capsules = db.timeCapsuleDao().getAllTimeCapsules()
                Log.d("NotificationWorker", "Worker started, found ${capsules.size} capsules")

                val now = System.currentTimeMillis()
                // חישוב תחילת היום וסיום היום
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val startOfTomorrow = calendar.timeInMillis

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                // מסננים רק את הקפסולות שפתוחות היום ושטרם נשלחה עליהן התראה
                val filteredCapsules = capsules.filter { capsule ->
                    capsule.openDate in startOfToday until startOfTomorrow &&
                            !capsule.notified &&
                            currentUserId != null &&
                            capsule.creatorId == currentUserId
                }

                Log.d("NotificationWorker", "Capsules to notify: ${filteredCapsules.size}")

                filteredCapsules.forEach { capsule ->
                    sendNotification(capsule.title, capsule.content)
                    val updatedCapsule = capsule.copy(notified = true)
                    db.timeCapsuleDao().insertTimeCapsule(updatedCapsule)
                }
                Result.success()
            } catch (e: Exception) {
                Log.e("NotificationWorker", "Error in doWork", e)
                Result.failure()
            }
        }
    }

    private fun sendNotification(title: String, content: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "time_capsule_channel"
        val channelName = "Time Capsule Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
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
