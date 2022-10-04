package com.catelt.mome.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.movie.*
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCaseImpl,
    private val getDiscoverAllMoviesUseCase: GetDiscoverAllMoviesUseCaseImpl,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCaseImpl,
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCaseImpl,
    private val getTopRatedMoviesUseCase: GetTopRatedMoviesUseCaseImpl,
    private val getMovieDetailsUseCaseImpl: GetMovieDetailsUseCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel(){
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val movieDetails: MutableLiveData<MovieDetails?> = MutableLiveData(null)

    val moviesState: StateFlow<MoviesState> = deviceLanguage.mapLatest { deviceLanguage ->
        MoviesState(
            nowPlaying = getNowPlayingMoviesUseCase(deviceLanguage, false).cachedIn(viewModelScope),
            discover = getDiscoverAllMoviesUseCase(deviceLanguage).cachedIn(viewModelScope),
            upcoming = getUpcomingMoviesUseCase(deviceLanguage).cachedIn(viewModelScope),
            trending = getTrendingMoviesUseCase(deviceLanguage).cachedIn(viewModelScope),
            topRated = getTopRatedMoviesUseCase(deviceLanguage).cachedIn(viewModelScope)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), MoviesState.default)

    suspend fun getMovieDetail(mediaID: Int){
        deviceLanguage.collectLatest {
            getMovieDetailsUseCaseImpl(mediaID,it).onSuccess {
                viewModelScope.launch {
                    movieDetails.postValue(data)
                }
            }.onFailure {
                onFailure(this)
            }.onException {
                onError(this)
            }
        }

    }
}