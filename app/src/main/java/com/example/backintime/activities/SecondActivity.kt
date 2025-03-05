package com.example.backintime.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.backintime.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.findNavController()

        navController?.let {
            bottomNavigationView.setupWithNavController(it)
        }
    }
}