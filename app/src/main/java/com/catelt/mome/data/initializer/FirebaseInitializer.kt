package com.catelt.mome.data.initializer

import android.app.Application
import com.google.firebase.FirebaseApp
import javax.inject.Inject

class FirebaseInitializer @Inject constructor() : AppInitializer {
    override fun init(application: Application) {
        FirebaseApp.initializeApp(application)
    }
}