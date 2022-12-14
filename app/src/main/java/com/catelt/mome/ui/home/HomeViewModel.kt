package com.catelt.mome.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.movie.Movie
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
import com.catelt.mome.domain.usecase.tvshow.*
import com.catelt.mome.utils.BUNDLE_ID_GENRE
import com.catelt.mome.utils.BUNDLE_IS_MOVIE
import com.catelt.mome.utils.ImageUrlParser
import com.catelt.mome.utils.SlugUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCaseImpl,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCaseImpl,
    private val getDiscoverAllMoviesUseCase: GetDiscoverAllMoviesUseCaseImpl,
    private val getPopularMoviesUseCaseImpl: GetPopularMoviesUseCaseImpl,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCaseImpl,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCaseImpl,
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCaseImpl,
    private val getOnTheAirTvShowsUseCase: GetOnTheAirTvShowsUseCaseImpl,
    private val getDiscoverAllTvShowsUseCase: GetDiscoverAllTvShowsUseCaseImpl,
    private val getTopRatedTvShowsUseCase: GetTopRatedTvShowsUseCaseImpl,
    private val getTrendingTvShowsUseCase: GetTrendingTvShowsUseCaseImpl,
    private val getAiringTodayTvShowsUseCase: GetAiringTodayTvShowsUseCaseImpl,
    private val getMediaDetailUserCaseImpl: GetMediaDetailUserCaseImpl,
    private val addMediaMyListUseCase: AddMediaMyListUseCaseImpl,
    private val removeMediaMyListUseCase: RemoveMediaMyListUseCaseImpl,
    private val checkMediaInMyListUseCase: CheckMediaInMyListUseCaseImpl,
    private val configRepository: ConfigRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()
    private val isMovie = MutableStateFlow(true)
    private val dataIsMovie = savedStateHandle.getLiveData<Boolean>(BUNDLE_IS_MOVIE)
    private val _genreId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_GENRE)
    val genreId = MutableStateFlow(0)

    val countStack = MutableStateFlow(0)

    var trailerMedia: List<Presentable> = emptyList()
    var positionNowPlaying = Random.nextInt(0, 10)

    private val isMyList = MutableStateFlow(false)
    val media = MutableStateFlow(
        Movie(
            id = 0,
            adult = null,
            backdropPath = null,
            popularity = null,
            posterPath = null,
            releaseDate = null,
            overview = "",
            genreIds = emptyList(),
            originalTitle = "",
            originalLanguage = "",
            title = "",
            voteCount = 0,
            video = false,
            voteAverage = 0F
        ) as Presentable
    )

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val moviesGenres: StateFlow<List<Genre>?> = configRepository.getMoviesGenres()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val moviesState: StateFlow<MoviesState> = deviceLanguage.mapLatest { deviceLanguage ->
        val id = _genreId.value ?: 0
        MoviesState(
            nowPlaying = getNowPlayingMoviesUseCase(deviceLanguage, false).cachedIn(viewModelScope),
            discover = getDiscoverAllMoviesUseCase(deviceLanguage,id).cachedIn(viewModelScope),
            popular = getPopularMoviesUseCaseImpl(deviceLanguage,id).cachedIn(viewModelScope),
            trending = getTrendingMoviesUseCase(deviceLanguage,id).cachedIn(viewModelScope),
            topRated = getTopRatedMoviesUseCase(deviceLanguage,id).cachedIn(viewModelScope)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), MoviesState.default)

    private val tvShowsState: StateFlow<TvShowsState> = deviceLanguage.mapLatest { deviceLanguage ->
        val id = _genreId.value ?: 0
        TvShowsState(
            onTheAir = getOnTheAirTvShowsUseCase(deviceLanguage, true)
                .cachedIn(viewModelScope),
            discover = getDiscoverAllTvShowsUseCase(deviceLanguage,id)
                .cachedIn(viewModelScope),
            topRated = getTopRatedTvShowsUseCase(deviceLanguage,id)
                .cachedIn(viewModelScope),
            trending = getTrendingTvShowsUseCase(deviceLanguage,id)
                .cachedIn(viewModelScope),
            airingToday = getAiringTodayTvShowsUseCase(deviceLanguage,id)
                .cachedIn(viewModelScope)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), TvShowsState.default)

    private val ophim: MutableStateFlow<OphimResponse?> = MutableStateFlow(null)

    val uiState: StateFlow<HomeUIState> = combine(
        isMovie, moviesState, tvShowsState, ophim, isMyList
    ) { isMovie, moviesState, tvShowsState, ophim, mylist ->
        if (isMovie) {
            HomeUIState(
                homeState = HomeState.MovieData(moviesState),
                ophim = ophim,
                isMyList = mylist
            )
        } else {
            HomeUIState(
                homeState = HomeState.TvShowData(tvShowsState),
                ophim = ophim,
                isMyList = mylist
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUIState.default)

    init {
        setIsMovie(dataIsMovie.value ?: true)

        viewModelScope.launch {

            media.collectLatest { presentable ->
                ophim.emit(null)
                isMyList.emit(false)
                launch {
                    checkMediaInMyList(presentable)
                }
//                launch {
//                    isMovie.collectLatest { isMovie ->
//                        if (isMovie) {
//                            launch {
//                                getMovieDetails(presentable.id)
//                            }
//                            launch {
//                                getMovieDetails(presentable.id, DeviceLanguage.default)
//                            }
//                        } else {
//                            launch {
//                                getTvShowDetail(presentable.id)
//                            }
//                            launch {
//                                getTvShowDetail(presentable.id, DeviceLanguage.default)
//                            }
//                        }
//                    }
//
//                }
            }
        }
    }

    fun setIsMovie(value: Boolean) {
        viewModelScope.launch {
            isMovie.emit(value)
        }
    }

    fun getIsMovie(): Boolean {
        return isMovie.value
    }

    fun setGenreId(value: Int){
        viewModelScope.launch {
            genreId.emit(value)
        }
    }

    private fun setCurrentMedia(presentable: Presentable) {
        viewModelScope.launch {
            media.emit(presentable)
        }
    }

    private suspend fun getMovieDetails(
        movieId: Int,
        deviceLanguage: DeviceLanguage = DeviceLanguage(
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

                movieDetails?.title?.let {
                    getMediaDetail(SlugUtils.slugify(movieDetails.title))
                }
            }
        }.onFailure {
            onFailure(this)
        }.onException {
            onError(this)
        }
    }

    private suspend fun getTvShowDetail(
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

                tvShowDetails?.title?.let {
                    getMediaDetail(SlugUtils.slugify(tvShowDetails.title))
                }
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
        val type = if (isMovie.value) MediaType.Movie else MediaType.Tv
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

    suspend fun checkMediaInMyList(presentable: Presentable) {
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

    fun addStack() {
        viewModelScope.launch {
            countStack.value.let {
                val new = it + 1
                countStack.emit(new)
            }
        }
    }

    fun popStack() {
        viewModelScope.launch {
            countStack.value.let {
                val new = if (it > 0) it - 1 else 0
                countStack.emit(new)
            }
        }
    }

    fun getCountStack(): Int {
        return countStack.value
    }

    fun randomMedia(isRandom: Boolean = true) {
        if (isRandom) {
            positionNowPlaying = Random.nextInt(0, 10)
        }
        if (trailerMedia.isNotEmpty()) {
            setCurrentMedia(trailerMedia[positionNowPlaying])
        }
    }

    fun getGenre(genreId: Int): String?{
        moviesGenres.value?.forEach {
            if (genreId == it.id) return it.name
        }
        return null
    }
}