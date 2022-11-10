package com.catelt.mome.ui.videoplayer

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.SeasonDetails
import com.catelt.mome.data.model.firebase.MovieFirebase
import com.catelt.mome.data.model.firebase.TimeAt
import com.catelt.mome.data.model.ophim.Media
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.GetMediaDetailUserCaseImpl
import com.catelt.mome.domain.usecase.firebase.AddWatchTimeAtUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.GetMovieFirebaseUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.GetWatchTimeAtUseCaseImpl
import com.catelt.mome.domain.usecase.tvshow.GetSeasonDetailsUseCaseImpl
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.BUNDLE_SLUG_MEDIA
import com.catelt.mome.utils.BUNDLE_TITLE_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getMediaDetailUserCase: GetMediaDetailUserCaseImpl,
    private val getSeasonDetailsUseCase: GetSeasonDetailsUseCaseImpl,
    private val getMovieFirebaseUseCase: GetMovieFirebaseUseCaseImpl,
    private val getWatchTimeAtUseCase: GetWatchTimeAtUseCaseImpl,
    private val addWatchTimeAtUseCase: AddWatchTimeAtUseCaseImpl,
    private val savedStateHandle: SavedStateHandle,
    private val configRepository: ConfigRepository,
) : BaseViewModel() {
    private val mediaId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_MEDIA)
    private val mediaSlug = savedStateHandle.getLiveData<String>(BUNDLE_SLUG_MEDIA)
    val mediaTitle = savedStateHandle.getLiveData<String>(BUNDLE_TITLE_MEDIA)

    var mBitmapList = MutableStateFlow<MutableList<Bitmap?>?>(null)

    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase()
    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val seasonDetails: MutableStateFlow<SeasonDetails?> = MutableStateFlow(null)

    //    private val episodes: MutableStateFlow<List<OphimEpisode>?> = MutableStateFlow(null)
    val episode: MutableStateFlow<MovieFirebase?> = MutableStateFlow(null)
    val movies = MutableStateFlow<List<Media>>(emptyList())

    val timeAt = MutableStateFlow<Long>(0)

    init {
        val id = mediaId.value ?: 0
        val slug = mediaSlug.value ?: ""
        val isMovie = mediaTitle.value != null


        viewModelScope.launch {
//            launch {
//                episodes.collectLatest {
//                    it?.let { list ->
//                        if (isMovie) {
//                            list[0].let { ophimEpisode ->
//                                val data = mutableListOf<Media>()
//                                val tvShowParcelable = Media(
//                                    numberEpisode = 0,
//                                    title = mediaTitle.value,
//                                    stillPath = "",
//                                    url = ophimEpisode.url,
//                                    overview = ""
//                                )
//                                data.add(tvShowParcelable)
//                                movies.emit(data)
//                            }
//                        } else {
//                            launch {
//                                seasonDetails.collectLatest { seasonDetails ->
//                                    seasonDetails?.let {
//                                        seasonDetails.episodes.let { episodes ->
//                                            val data = mutableListOf<Media>()
//                                            list.forEachIndexed { index, ophimEpisode ->
//                                                val tvShowParcelable = Media(
//                                                    numberEpisode = index + 1,
//                                                    title = episodes[index].name,
//                                                    stillPath = episodes[index].stillPath,
//                                                    url = ophimEpisode.url,
//                                                    overview = episodes[index].overview
//                                                )
//                                                data.add(tvShowParcelable)
//                                            }
//                                            movies.emit(data)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }

//            deviceLanguage.collectLatest { deviceLanguage ->
//                if (!isMovie) {
//                    launch {
//                        getSeasonDetails(id, deviceLanguage)
//                    }
//                }
//                launch {
//                    getMediaDetail(slug)
//                }
//            }
            launch {
                getMovieFirebase(id)
            }
            launch {
                getWatchTimeAt(id)
            }

        }
    }


//    var job: Job? = null
//    fun getVideoFrame(uri: String?) {
//        job?.cancel()
//        job = viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val retriever = FFmpegMediaMetadataRetriever()
//                val thumbnailList = mutableListOf<Bitmap?>()
//
//                retriever.setDataSource(uri)
//
//                val videoLengthInMs =
//                    (retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)!!
//                        .toInt() * 1000).toLong()
//                val internal = videoLengthInMs / TIME_PREVIEW
//
//                for (i in 0..internal) {
//                    val bitmap = retriever.getFrameAtTime(
//                        i * TIME_PREVIEW + TIME_PREVIEW / MINUS_DELAY,
//                        FFmpegMediaMetadataRetriever.OPTION_CLOSEST
//                    )
//                    thumbnailList.add(bitmap)
//
//                    viewModelScope.launch {
//                        mBitmapList.emit(thumbnailList)
//                    }
//                }
//                retriever.release()
//            } catch (ex: RuntimeException) {
//                ex.printStackTrace()
//            }
//        }
//
//    }

    private suspend fun getSeasonDetails(tvShowId: Int, deviceLanguage: DeviceLanguage) {
        getSeasonDetailsUseCase(
            tvShowId = tvShowId,
            seasonNumber = 1,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                seasonDetails.emit(data)
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

//    private suspend fun getMediaDetail(
//        slugName: String,
//    ) {
//        getMediaDetailUserCaseImpl(
//            slugName = slugName,
//        ).onSuccess {
//            viewModelScope.launch {
//                if (data?.status == true) {
//                    episodes.emit(data.episodeResponses[0].episodes)
//                }
//            }
//        }.onFailure {
//            onFailure(this)
//        }.onException {
//            onError(this)
//        }
//    }

    private fun getMovieFirebase(mediaId: Int) {
        viewModelScope.launch {
            getMovieFirebaseUseCase(mediaId).collectLatest {
                it.handle(
                    success = { data ->
                        data.url = "${BASE_URL_MOVIE}${data.url}"
                        viewModelScope.launch {
                            episode.emit(data)
                        }
                    }
                )
            }
        }

    }

    private suspend fun getWatchTimeAt(mediaId: Int) {
        getWatchTimeAtUseCase(mediaId).collectLatest {
            it.handle(
                success = { data ->
                    viewModelScope.launch {
                        timeAt.emit(data.time)
                    }
                }
            )
        }
    }

    suspend fun addWatchTimeAt(timeAt: Long) {
        addWatchTimeAtUseCase(
            mediaId = mediaId.value ?: 0,
            timeAt = TimeAt(timeAt),
        ).collectLatest {
            it.handle(
                success = {
                    println("------------")
                    println(it)
                }
            )
        }
    }

    companion object {
        private const val BASE_URL_MOVIE = "https://4420-27-74-244-188.ap.ngrok.io/api/v1/video/"
        private const val MINUS_DELAY = 2L
        const val TIME_PREVIEW = 60 * MINUS_DELAY * 1000000L
    }
}