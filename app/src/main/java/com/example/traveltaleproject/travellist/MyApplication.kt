package com.example.traveltaleproject.travellist

import android.app.Application
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyBcMGGKyO_NG1gRlNwJni0fxpwMLTbIGW4")
    }
}
