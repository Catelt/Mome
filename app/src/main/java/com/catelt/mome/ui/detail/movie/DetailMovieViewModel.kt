package com.catelt.mome.ui.detail.movie

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.movie.MovieCollection
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.ophim.OphimResponse
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.GetMediaDetailUserCaseImpl
import com.catelt.mome.domain.usecase.firebase.AddMediaMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.CheckMediaInMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.RemoveMediaMyListUseCaseImpl
import com.catelt.mome.domain.usecase.movie.*
import com.catelt.mome.utils.BUNDLE_ID_MEDIA
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DetailMovieViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getRelatedMoviesUseCase: GetRelatedMoviesOfTypeUseCaseImpl,
    private val getMovieCreditsUseCase: GetMovieCreditUseCaseImpl,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCaseImpl,
    private val getMoviesVideosUseCase: GetMovieVideosUseCaseImpl,
    private val getMovieBackdropsUseCase: GetMovieBackdropsUseCaseImpl,
    private val getMovieCollectionUseCase: GetMovieCollectionUseCaseImpl,
    private val getOtherDirectorMoviesUseCase: GetOtherDirectorMoviesUseCaseImpl,
    private val getMediaDetailUserCaseImpl: GetMediaDetailUserCaseImpl,
    private val addMediaMyListUseCase: AddMediaMyListUseCaseImpl,
    private val removeMediaMyListUseCase: RemoveMediaMyListUseCaseImpl,
    private val checkMediaInMyListUseCase: CheckMediaInMyListUseCaseImpl,
    private val configRepository: ConfigRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val movieId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_MEDIA)
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase()
    private val watchAtTime: MutableStateFlow<Date?> = MutableStateFlow(null)
    private val _movieDetails: MutableStateFlow<MovieDetails?> = MutableStateFlow(null)
    private val movieDetails: StateFlow<MovieDetails?> =
        _movieDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), null)
    val isMyList = MutableStateFlow(false)

    private val credits: MutableStateFlow<Credits?> = MutableStateFlow(null)
    private val movieBackdrops: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    private val movieCollection: MutableStateFlow<MovieCollection?> = MutableStateFlow(null)
    private val otherDirectorMovies: MutableStateFlow<DirectorMovies> = MutableStateFlow(
        DirectorMovies.default
    )

    private val videos: MutableStateFlow<List<Video>?> = MutableStateFlow(null)
    private val ophim: MutableStateFlow<OphimResponse?> = MutableStateFlow(null)

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val associatedMovies: StateFlow<AssociatedMovies> = combine(
        deviceLanguage, movieCollection, otherDirectorMovies
    ) { deviceLanguage, collection, otherDirectorMovies ->
        AssociatedMovies(
            collection = collection,
            similar = getRelatedMoviesUseCase(
                movieId = movieId.value ?: 0,
                type = RelationType.Similar,
                deviceLanguage = deviceLanguage
            ).cachedIn(viewModelScope),
            recommendations = getRelatedMoviesUseCase(
                movieId = movieId.value ?: 0,
                type = RelationType.Recommended,
                deviceLanguage = deviceLanguage
            ).cachedIn(viewModelScope),
            directorMovies = otherDirectorMovies,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), AssociatedMovies.default)


    private val associatedContent: StateFlow<AssociatedContent> = combine(
        movieBackdrops, videos, credits
    ) { backdrops, videos, credits  ->
        AssociatedContent(
            backdrops = backdrops,
            videos = videos,
            credits = credits,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), AssociatedContent.default)

    val uiState: StateFlow<MovieDetailsScreenUIState> = combine(
        movieDetails, associatedMovies, associatedContent, ophim, error
    ) { details, associatedMovies, visualContent, ophim, error ->
        MovieDetailsScreenUIState(
            movieDetails = details,
            associatedMovies = associatedMovies,
            associatedContent = visualContent,
            ophim = ophim,
            error = error
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        MovieDetailsScreenUIState.getDefault()
    )

    init {
        getMovieInfo()
    }

    private fun getMovieInfo() {
        viewModelScope.launch {
            val movieId = movieId.value ?: 0

            launch {
                getMovieBackdrops(movieId)
            }

            deviceLanguage.collectLatest { deviceLanguage ->
                launch {
                    getMovieDetails(movieId, deviceLanguage)
                }
                launch {
                    getMovieCredits(movieId, deviceLanguage)
                }
                launch {
                    getMovieVideos(movieId, deviceLanguage)
                }
//                launch {
//                    getMovieDetails(movieId)
//                }
            }
        }

        startRefreshingWatchAtTime()
    }

    private fun startRefreshingWatchAtTime() {
        viewModelScope.launch {
            _movieDetails.collectLatest { details ->
                while (this@launch.isActive) {
                    details?.runtime?.let { runtime ->
                        if (runtime > 0) {
                            runtime.minutes.toComponents { hours, minutes, _, _ ->
                                val time = Calendar.getInstance().apply {
                                    time = Date()

                                    add(Calendar.HOUR, hours.toInt())
                                    add(Calendar.MINUTE, minutes)
                                }.time

                                watchAtTime.emit(time)
                            }
                        }
                    }

                    delay(10.seconds)
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
                val movieDetails = data
                if (deviceLanguage.languageCode != "vi") {
                    _movieDetails.emit(movieDetails)
                    launch {
                        movieDetails?.let {
                            checkMediaInMyList(it)
                        }
                    }
                }

//                movieDetails?.title?.let {
//                    getMediaDetail(SlugUtils.slugify(movieDetails.title))
//                }

                data?.collection?.id?.let { collectionId ->
                    getMovieCollection(
                        collectionId = collectionId,
                        deviceLanguage = deviceLanguage
                    )
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getMovieCredits(movieId: Int, deviceLanguage: DeviceLanguage) {
        getMovieCreditsUseCase(
            movieId = movieId,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                credits.emit(data)

                val mainDirector = data?.getDirector()

                if (mainDirector != null) {
                    getOtherDirectorMovies(
                        mainDirector = mainDirector,
                        deviceLanguage = deviceLanguage
                    )
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getMovieBackdrops(movieId: Int) {
        getMovieBackdropsUseCase(movieId).onSuccess {
            viewModelScope.launch {
                movieBackdrops.emit(data ?: emptyList())
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getMovieCollection(collectionId: Int, deviceLanguage: DeviceLanguage) {
        getMovieCollectionUseCase(
            collectionId = collectionId,
            deviceLanguage = deviceLanguage
        ).onSuccess {
            viewModelScope.launch {
                movieCollection.emit(data)
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getMovieVideos(movieId: Int, deviceLanguage: DeviceLanguage) {
        getMoviesVideosUseCase(
            movieId = movieId,
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

    private suspend fun getOtherDirectorMovies(
        mainDirector: CrewMember,
        deviceLanguage: DeviceLanguage
    ) {
        val movies = getOtherDirectorMoviesUseCase(
            mainDirector = mainDirector,
            deviceLanguage = deviceLanguage
        ).cachedIn(viewModelScope)

        val directorMovies = DirectorMovies(
            directorName = mainDirector.name,
            movies = movies
        )

        otherDirectorMovies.emit(directorMovies)
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
            type = MediaType.Movie
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
