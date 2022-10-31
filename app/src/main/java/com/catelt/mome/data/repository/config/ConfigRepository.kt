package com.catelt.mome.data.repository.config

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.model.ProviderSource
import com.catelt.mome.utils.ImageUrlParser
import kotlinx.coroutines.flow.Flow

interface ConfigRepository {
    fun isInitialized(): Flow<Boolean>

    fun updateLocale()

    fun getSpeechToTextAvailable(): Flow<Boolean>

    fun getDeviceLanguage(): Flow<DeviceLanguage>

    fun getImageUrlParser(): Flow<ImageUrlParser?>

    fun getMoviesGenres(): Flow<List<Genre>>

    fun getAllMoviesWatchProviders(): Flow<List<ProviderSource>>
}