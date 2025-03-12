package com.example.backintime.utils

import android.app.Application
import android.util.Log
import com.google.android.gms.security.ProviderInstaller

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            Log.e("MyApplication", "ProviderInstaller failed", e)
        }
    }
}
