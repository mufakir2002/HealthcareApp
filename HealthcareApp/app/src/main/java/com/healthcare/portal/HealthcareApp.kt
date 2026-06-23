package com.healthcare.portal

import android.app.Application

class HealthcareApp : Application() {
    companion object {
        lateinit var instance: HealthcareApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
