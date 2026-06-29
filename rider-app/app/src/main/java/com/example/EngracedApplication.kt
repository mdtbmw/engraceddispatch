package com.example

import android.app.Application

class EngracedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Mapbox disabled - add MAPBOX_ACCESS_TOKEN to local.properties then uncomment:
        // MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    }
}
