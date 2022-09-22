package com.catelt.mome.data.paging

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import com.catelt.mome.data.model.Config
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.model.ProviderSource
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelper
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.remote.api.request
import com.catelt.mome.utils.ImageUrlParser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ConfigDataSource @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val externalScope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiMovieHelper: TmdbMoviesApiHelper,
) {
    private val _config: MutableStateFlow<Config?> = MutableStateFlow(null)

    @SuppressLint("QueryPermissionsNeeded")
    val speechToTextAvailable: Flow<Boolean> = flow {
        val packageManager = context.packageManager
        val activities: List<*> = packageManager.queryIntentActivities(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),
            0
        )

        emit(activities.isNotEmpty())
    }.flowOn(defaultDispatcher)


    private val _deviceLanguage: MutableStateFlow<DeviceLanguage> =
        MutableStateFlow(getCurrentDeviceLanguage())
    val deviceLanguage: StateFlow<DeviceLanguage> = _deviceLanguage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val imageUrlParser: Flow<ImageUrlParser?> = _config.mapLatest { config ->
        if (config != null) {
            ImageUrlParser(config.imagesConfig)
        } else null
    }.flowOn(defaultDispatcher)

    private val _movieGenres: MutableStateFlow<List<Genre>> = MutableStateFlow(emptyList())
    val movieGenres: StateFlow<List<Genre>> = _movieGenres.asStateFlow()

    private val _movieWatchProviders: MutableStateFlow<List<ProviderSource>> = MutableStateFlow(
        emptyList()
    )
    val movieWatchProviders: StateFlow<List<ProviderSource>> = _movieWatchProviders.asStateFlow()

    val isInitialized: StateFlow<Boolean> = combine(
        _config, _movieGenres, _movieWatchProviders
    ) { imageUrlParser, movieGenres, movieWatchProviders ->
        val imageUrlParserInit = imageUrlParser != null
        val movieGenresInit = movieGenres.isNotEmpty()
        val movieWatchProvidersInit = movieWatchProviders.isNotEmpty()

        listOf(
            imageUrlParserInit,
            movieGenresInit,
            movieWatchProvidersInit,
        ).all { init -> init }
    }.stateIn(externalScope, SharingStarted.WhileSubscribed(10), false)

    fun init() {
        apiMovieHelper.getConfig().request { response ->
            response.onSuccess {
                externalScope.launch(defaultDispatcher) {
                    val config = data
                    _config.emit(config)
                }
            }.onException {
                FirebaseCrashlytics.getInstance().recordException(exception)
            }
        }

        externalScope.launch(defaultDispatcher) {
            deviceLanguage.collectLatest { deviceLanguage ->
                apiMovieHelper.getMoviesGenres(isoCode = deviceLanguage.languageCode)
                    .request { response ->
                        response.onSuccess {
                            externalScope.launch(defaultDispatcher) {
                                val movieGenres = data?.genres

                                _movieGenres.emit(movieGenres ?: emptyList())
                            }
                        }.onException {
                            FirebaseCrashlytics.getInstance().recordException(exception)
                        }
                    }

                apiMovieHelper.getAllMoviesWatchProviders(
                    isoCode = deviceLanguage.languageCode,
                    region = deviceLanguage.region
                ).request { response ->
                    response.onSuccess {
                        externalScope.launch(defaultDispatcher) {
                            val watchProviders = data?.results?.sortedBy { provider ->
                                provider.displayPriority
                            }

                            _movieWatchProviders.emit(watchProviders ?: emptyList())
                        }
                    }.onException {
                        FirebaseCrashlytics.getInstance().recordException(exception)
                    }
                }
            }
        }
    }

    fun updateLocale() {
        externalScope.launch {
            val deviceLanguage = getCurrentDeviceLanguage()
            _deviceLanguage.emit(deviceLanguage)
        }
    }

    private fun getCurrentDeviceLanguage(): DeviceLanguage {
        val locale = Locale.getDefault()

        val languageCode = locale.toLanguageTag().ifEmpty { DeviceLanguage.default.languageCode }
        val region = locale.country.ifEmpty { DeviceLanguage.default.region }

        return DeviceLanguage(
            languageCode = languageCode,
            region = region
        )
    }

}