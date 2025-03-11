package com.example.backintime.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.R

class MainActivity : AppCompatActivity() {
    private val firebaseModel = FirebaseModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (firebaseModel.isUserLoggedIn()) {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
    }
}
