package com.example.getyourride

import android.app.Application
import org.osmdroid.config.Configuration

class GetYourRideApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}