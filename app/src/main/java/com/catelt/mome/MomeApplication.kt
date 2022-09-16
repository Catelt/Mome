package com.catelt.mome

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.catelt.mome.data.initializer.AppInitializers
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MomeApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var initializers: AppInitializers

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initializers.init(this)
    }

    override fun newImageLoader() = imageLoader
}