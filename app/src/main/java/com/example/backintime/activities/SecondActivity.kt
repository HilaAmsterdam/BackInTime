package com.example.backintime.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.backintime.R
import com.example.backintime.worker.NotificationWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class SecondActivity : AppCompatActivity() {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        requestNotificationPermissionIfNeeded()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()

        navController?.let {
            bottomNavigationView.setupWithNavController(it)
        }

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TimeCapsuleNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        val testWork = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(this).enqueue(testWork)
    }
}
