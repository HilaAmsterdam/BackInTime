package com.example.backintime.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.backintime.Model.FirebaseModel
import com.example.backintime.R
import com.example.backintime.viewModel.ProgressViewModel
import androidx.activity.viewModels
import android.view.View
import android.widget.ProgressBar

class MainActivity : AppCompatActivity() {
    private val firebaseModel = FirebaseModel()
    private val progressViewModel: ProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressViewModel.isLoading.observe(this) { isLoading ->
            findViewById<ProgressBar>(R.id.mainProgressIndicator)?.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        if (firebaseModel.isUserLoggedIn()) {
            startActivity(Intent(this, SecondActivity::class.java))
            finish()
        }
    }
}
