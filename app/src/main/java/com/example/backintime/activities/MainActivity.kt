package com.example.backintime

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.backintime.model.firebase.FirebaseModel

class MainActivity : AppCompatActivity() {
    private val firebaseModel = FirebaseModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // קבל את ה-NavHostFragment בצורה בטוחה
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        if (navHostFragment != null) {
            val navController: NavController = navHostFragment.navController

            if (firebaseModel.isUserLoggedIn()) {
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}
