package com.catelt.mome.data.repository.config

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.model.ProviderSource
import com.catelt.mome.data.paging.ConfigDataSource
import com.catelt.mome.utils.ImageUrlParser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val configDataSource: ConfigDataSource
) : ConfigRepository {
    override fun isInitialized(): Flow<Boolean> {
        return configDataSource.isInitialized
    }

    override fun updateLocale() {
        return configDataSource.updateLocale()
    }

    override fun getSpeechToTextAvailable(): Flow<Boolean> {
        return configDataSource.speechToTextAvailable
    }

    override fun getDeviceLanguage(): Flow<DeviceLanguage> {
        return configDataSource.deviceLanguage
    }

    override fun getImageUrlParser(): Flow<ImageUrlParser?> {
        return configDataSource.imageUrlParser
    }

    override fun getMoviesGenres(): Flow<List<Genre>> {
        return configDataSource.movieGenres
    }

    override fun getAllMoviesWatchProviders(): Flow<List<ProviderSource>> {
        return configDataSource.movieWatchProviders
    }
}