package com.catelt.mome.ui.bottomsheet

import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DetailPresentable
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.ophim.OphimMovie
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
import com.catelt.mome.domain.usecase.movie.GetMovieDetailsUseCaseImpl
import com.catelt.mome.domain.usecase.tvshow.GetTvShowDetailsUseCaseImpl
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.SlugUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MediaDetailsBottomViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCaseImpl,
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCaseImpl,
    private val addMediaMyListUseCase: AddMediaMyListUseCaseImpl,
    private val removeMediaMyListUseCase: RemoveMediaMyListUseCaseImpl,
    private val checkMediaInMyListUseCase: CheckMediaInMyListUseCaseImpl,
    private val getMediaDetailUserCase: GetMediaDetailUserCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()

    private val isMyList = MutableStateFlow(false)
    private val movieDetails: MutableStateFlow<MovieDetails?> = MutableStateFlow(null)
    private val tvShowsDetails: MutableStateFlow<TvShowDetails?> = MutableStateFlow(null)
    private val ophim: MutableStateFlow<OphimMovie?> = MutableStateFlow(null)

    private val mediaId = MutableStateFlow<Int?>(null)
    private val isMovie = MutableStateFlow<Boolean?>(null)

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val uiState: StateFlow<MediaDetailBottomState> = combine(
        movieDetails, tvShowsDetails, isMyList, ophim
    ) { movieDetails, tvShowsDetails, isMyList, ophim ->
        MediaDetailBottomState(
            movieDetails = movieDetails,
            tvShowsDetails = tvShowsDetails,
            isMyList = isMyList,
            ophim = ophim,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        MediaDetailBottomState.getDefault()
    )

    init {
        viewModelScope.launch {
            mediaId.collectLatest { it ->
                it?.let { id ->
                    isMovie.collectLatest {
                        it?.let { isMovie ->
                            viewModelScope.launch {
                                deviceLanguage.collectLatest { deviceLanguage ->
                                    if (isMovie){
                                        launch {
                                            getMovieDetails(id,deviceLanguage)
                                        }
                                        launch {
                                            getMovieDetails(id)
                                        }
                                    }
                                    else{
                                        launch {
                                            getTvShowDetails(id,deviceLanguage)
                                        }
                                        launch {
                                            getTvShowDetails(id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    private suspend fun getMovieDetails(
        movieId: Int, deviceLanguage: DeviceLanguage = DeviceLanguage(
            region = "VN",
            languageCode = "vi"
        )
    ) {
        getMovieDetailsUseCase(
            movieId = movieId,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                if (deviceLanguage.languageCode != "vi") {
                    movieDetails.emit(data)
                    launch {
                        data?.let {
                            checkMediaInMyList(it)
                        }
                    }
                }

                data?.title?.let {
                    getMediaDetail(SlugUtils.slugify(data.title))
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
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
                if (deviceLanguage.languageCode != "vi") {
                    tvShowsDetails.emit(data)

                    launch {
                        data?.let {
                            checkMediaInMyList(it)
                        }
                    }
                }

                data?.name?.let {
                    getMediaDetail(SlugUtils.slugify(data.name))
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    fun onAddMediaClick(presentable: DetailPresentable,isMovie: Boolean) {
        val type = if (isMovie) MediaType.Movie else MediaType.Tv
        val data = Media(
            id = presentable.id,
            title = presentable.title,
            posterPath = presentable.posterPath,
            type = type
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

    fun onRemoveClick(presentable: DetailPresentable) {
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

    private suspend fun checkMediaInMyList(presentable: DetailPresentable) {
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

    private suspend fun getMediaDetail(
        slugName: String
    ) {
        getMediaDetailUserCase(
            slugName = slugName,
        ).onSuccess {
            viewModelScope.launch {
                if (data?.status == true) {
                    if (ophim.value == null) {
                        ophim.emit(data.movie)
                    }
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    fun setMediaId(value: Int){
        viewModelScope.launch {
            mediaId.emit(value)
        }
    }

    fun setIsMovie(value: Boolean){
        viewModelScope.launch {
            isMovie.emit(value)
        }
    }
}