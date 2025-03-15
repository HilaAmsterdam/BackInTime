package com.example.backintime.utils

import android.app.Application
import android.util.Log
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.auth.FirebaseAuth

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseAuth.getInstance().setLanguageCode("he")
        Log.d("MyApplication", "Firebase language code: ${FirebaseAuth.getInstance().languageCode}")

        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "ProviderInstaller failed", e)
        }
    }
}
