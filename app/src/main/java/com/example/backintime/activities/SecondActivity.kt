package com.example.backintime.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.backintime.viewModel.ProgressViewModel
import com.example.backintime.worker.NotificationWorker
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class SecondActivity : AppCompatActivity() {
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    private val progressViewModel: ProgressViewModel by viewModels()


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


    private fun simulateNewCapsuleInsertion() {

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        requestNotificationPermissionIfNeeded()

        progressViewModel.isLoading.observe(this) { isLoading ->
            findViewById<ProgressBar>(R.id.mainProgressIndicator)?.visibility =
                if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()
        navController?.let {
            bottomNavigationView.setupWithNavController(it)
        }


        val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TimeCapsuleNotification",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )

        simulateNewCapsuleInsertion()
    }
}
