package com.catelt.mome.di

import com.catelt.mome.data.initializer.AppInitializer
import com.catelt.mome.data.initializer.FirebaseInitializer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModuleBinds {

    @Binds
    @IntoSet
    abstract fun provideFirebaseInitializer(
        initializer: FirebaseInitializer
    ): AppInitializer

}