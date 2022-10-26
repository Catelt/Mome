package com.catelt.mome.ui.videoplayer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.SeasonDetails
import com.catelt.mome.data.model.ophim.Media
import com.catelt.mome.data.model.ophim.OphimEpisode
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.GetMediaDetailUserCaseImpl
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
    private val getMediaDetailUserCaseImpl: GetMediaDetailUserCaseImpl,
    private val getSeasonDetailsUseCase: GetSeasonDetailsUseCaseImpl,
    private val savedStateHandle: SavedStateHandle,
    private val configRepository: ConfigRepository,
) : BaseViewModel() {
    private val mediaId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_MEDIA)
    private val mediaSlug = savedStateHandle.getLiveData<String>(BUNDLE_SLUG_MEDIA)
    private val mediaTitle = savedStateHandle.getLiveData<String>(BUNDLE_TITLE_MEDIA)

    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase()
    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val seasonDetails: MutableStateFlow<SeasonDetails?> = MutableStateFlow(null)
    private val episodes: MutableStateFlow<List<OphimEpisode>?> = MutableStateFlow(null)
    val movies = MutableStateFlow<List<Media>>(emptyList())

    init {
        val id = mediaId.value ?: 0
        val slug = mediaSlug.value ?: ""
        val isMovie = mediaTitle.value != null


        viewModelScope.launch {
            launch {
                episodes.collectLatest {
                    it?.let { list ->
                        if (isMovie) {
                            list[0].let { ophimEpisode ->
                                val data = mutableListOf<Media>()
                                val tvShowParcelable = Media(
                                    numberEpisode = 0,
                                    title = mediaTitle.value,
                                    stillPath = "",
                                    url = ophimEpisode.url,
                                    overview = ""
                                )
                                data.add(tvShowParcelable)
                                movies.emit(data)
                            }
                        } else {
                            launch {
                                seasonDetails.collectLatest { seasonDetails ->
                                    seasonDetails?.let {
                                        seasonDetails.episodes.let { episodes ->
                                            val data = mutableListOf<Media>()
                                            list.forEachIndexed { index, ophimEpisode ->
                                                val tvShowParcelable = Media(
                                                    numberEpisode = index + 1,
                                                    title = episodes[index].name,
                                                    stillPath = episodes[index].stillPath,
                                                    url = ophimEpisode.url,
                                                    overview = episodes[index].overview
                                                )
                                                data.add(tvShowParcelable)
                                            }
                                            movies.emit(data)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            deviceLanguage.collectLatest { deviceLanguage ->
                if (!isMovie) {
                    launch {
                        getSeasonDetails(id, deviceLanguage)
                    }
                }
                launch {
                    getMediaDetail(slug)
                }
            }

        }
    }

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

    private suspend fun getMediaDetail(
        slugName: String,
    ) {
        getMediaDetailUserCaseImpl(
            slugName = slugName,
        ).onSuccess {
            viewModelScope.launch {
                if (data?.status == true) {
                    episodes.emit(data.episodeResponses[0].episodes)
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

}