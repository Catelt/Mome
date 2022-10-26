package com.catelt.mome.ui.detail.tvshow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.ophim.OphimResponse
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.GetMediaDetailUserCaseImpl
import com.catelt.mome.domain.usecase.firebase.AddMediaMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.CheckMediaInMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.RemoveMediaMyListUseCaseImpl
import com.catelt.mome.domain.usecase.tvshow.*
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.SlugUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailTvShowViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCaseImpl,
    private val getRelatedTvShowsOfTypeUseCase: GetRelatedTvShowsOfTypeUseCaseImpl,
    private val getTvShowImagesUseCase: GetTvShowImagesUseCaseImpl,
    private val getTvShowVideosUseCase: GetTvShowVideosUseCaseImpl,
    private val getSeasonDetailsUseCase: GetSeasonDetailsUseCaseImpl,
    private val getSeasonCreditsUseCase: GetSeasonCreditsUseCaseImpl,
    private val getMediaDetailUserCaseImpl: GetMediaDetailUserCaseImpl,
    private val addMediaMyListUseCase: AddMediaMyListUseCaseImpl,
    private val removeMediaMyListUseCase: RemoveMediaMyListUseCaseImpl,
    private val checkMediaInMyListUseCase: CheckMediaInMyListUseCaseImpl,
    private val configRepository: ConfigRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val tvShowId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_MEDIA)
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase()
    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isMyList = MutableStateFlow(false)

    private val _tvShowDetails: MutableStateFlow<TvShowDetails?> = MutableStateFlow(null)
    private val tvShowDetails: StateFlow<TvShowDetails?> =
        _tvShowDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), null)

    private val credits: MutableStateFlow<AggregatedCredits?> = MutableStateFlow(null)
    private val tvShowBackdrops: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    private val videos: MutableStateFlow<List<Video>?> = MutableStateFlow(null)
    private val ophim: MutableStateFlow<OphimResponse?> = MutableStateFlow(null)

    private val seasonDetails: MutableStateFlow<SeasonDetails?> = MutableStateFlow(null)

    private val associatedTvShow: StateFlow<AssociatedTvShow> = combine(
        deviceLanguage, seasonDetails
    ) { deviceLanguage, seasonDetail ->
        AssociatedTvShow(
            similar = getRelatedTvShowsOfTypeUseCase(
                tvShowId = tvShowId.value ?: 0,
                type = RelationType.Similar,
                deviceLanguage = deviceLanguage
            ).cachedIn(viewModelScope),
            recommendations = getRelatedTvShowsOfTypeUseCase(
                tvShowId = tvShowId.value ?: 0,
                type = RelationType.Recommended,
                deviceLanguage = deviceLanguage
            ).cachedIn(viewModelScope),
            seasonDetails = seasonDetail,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), AssociatedTvShow.default)


    private val associatedContent: StateFlow<AssociatedContentTvShow> = combine(
        tvShowBackdrops, videos, credits, ophim
    ) { backdrops, videos, credits, episodes ->
        AssociatedContentTvShow(
            backdrops = backdrops,
            videos = videos,
            credits = credits,
            ophim = episodes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), AssociatedContentTvShow.default)

    val uiState: StateFlow<DetailTvShowUIState> = combine(
        tvShowDetails, associatedTvShow, associatedContent, error
    ) { details, associatedTvSeries, visualContent, error ->
        DetailTvShowUIState(
            tvShowDetails = details,
            associatedTvShow = associatedTvSeries,
            associatedContentTvShow = visualContent,
            error = error
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DetailTvShowUIState.getDefault()
    )

    init {
        getTvShowsInfo()
    }

    private fun getTvShowsInfo() {

        viewModelScope.launch {
            val tvShowId = tvShowId.value ?: 0

            launch {
                getTvShowImages(tvShowId)
            }

            deviceLanguage.collectLatest { deviceLanguage ->
                launch {
                    getTvShowDetails(
                        tvShowId = tvShowId,
                        deviceLanguage = deviceLanguage
                    )
                }
                launch {
                    getSeasonDetails(
                        tvShowId = tvShowId,
                        deviceLanguage = deviceLanguage
                    )
                }
                launch {
                    getTvShowCredits(
                        tvShowId = tvShowId,
                        deviceLanguage = deviceLanguage,
                    )
                }
                launch {
                    getTvShowVideos(
                        tvShowId = tvShowId,
                        deviceLanguage = deviceLanguage
                    )
                }
                launch {
                    getTvShowDetails(
                        tvShowId = tvShowId
                    )
                }
            }
        }
    }

    private suspend fun getTvShowDetails(
        tvShowId: Int, deviceLanguage: DeviceLanguage = DeviceLanguage(
            region = "VN",
            languageCode = "vi"
        )
    ) {
        getTvShowDetailsUseCase(
            tvShowId = tvShowId,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                val tvShowDetails = data
                if (deviceLanguage.languageCode != "vi") {
                   _tvShowDetails.emit(tvShowDetails)

                    launch {
                        tvShowDetails?.let {
                            checkMediaInMyList(it)
                        }
                    }
                }
                tvShowDetails?.name?.let {
                    getMediaDetail(SlugUtils.slugify(tvShowDetails.name))
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getTvShowImages(tvShowId: Int) {
        getTvShowImagesUseCase(tvShowId)
            .onSuccess {
                viewModelScope.launch {
                    tvShowBackdrops.emit(data ?: emptyList())
                }
            }.onFailure {
                onFailure(this)
            }.onException {
                onError(this)
            }
    }

    private suspend fun getTvShowVideos(tvShowId: Int, deviceLanguage: DeviceLanguage) {
        getTvShowVideosUseCase(
            tvShowId = tvShowId,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                videos.emit(data ?: emptyList())
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
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

    private suspend fun getTvShowCredits(tvShowId: Int, deviceLanguage: DeviceLanguage) {
        getSeasonCreditsUseCase(
            tvShowId = tvShowId,
            seasonNumber = 1,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                credits.emit(data)
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getMediaDetail(
        slugName: String
    ) {
        getMediaDetailUserCaseImpl(
            slugName = slugName,
        ).onSuccess {
            viewModelScope.launch {
                if (data?.status == true) {
                    if (ophim.value == null) {
                        ophim.emit(data)
                    }
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    fun onAddMediaClick(presentable: Presentable) {
        val data = Media(
            id = presentable.id,
            title = presentable.title,
            posterPath = presentable.posterPath,
            type = MediaType.Tv
        )
        viewModelScope.launch {
            addMediaMyListUseCase.invoke(data).collectLatest {
                it.handle(
                    success = { data ->
                        data?.let {
                            toastMessage.postValue(data)
                        }
                    }
                )
            }
        }
    }

    fun onRemoveClick(presentable: Presentable) {
        viewModelScope.launch {
            removeMediaMyListUseCase(presentable.id).collectLatest {
                it.handle(
                    success = { data ->
                        data?.let {
                            toastMessage.postValue(data)
                        }
                    }
                )
            }
        }
    }

    private suspend fun checkMediaInMyList(presentable: Presentable) {
        checkMediaInMyListUseCase(presentable.id).collectLatest {
            it.handle(
                success = { isExisted ->
                    viewModelScope.launch {
                        isMyList.emit(isExisted)
                    }
                }
            )
        }
    }
}