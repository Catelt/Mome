package com.catelt.mome.ui.bottomsheet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.movie.GetMovieDetailsUseCaseImpl
import com.catelt.mome.domain.usecase.tvshow.GetTvShowDetailsUseCaseImpl
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaDetailsBottomViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getMovieDetailsUseCaseImpl: GetMovieDetailsUseCaseImpl,
    private val getTvShowDetailsUseCaseImpl: GetTvShowDetailsUseCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()

    val movieDetails: MutableLiveData<MovieDetails?> = MutableLiveData(null)
    val tvShowsDetails: MutableLiveData<TvShowDetails?> = MutableLiveData(null)

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun getMovieDetail(mediaID: Int) {
        deviceLanguage.collectLatest {
            getMovieDetailsUseCaseImpl(mediaID, it).onSuccess {
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

    suspend fun getTvshowDetail(mediaID: Int) {
        deviceLanguage.collectLatest {
            getTvShowDetailsUseCaseImpl(mediaID, it).onSuccess {
                viewModelScope.launch {
                    tvShowsDetails.postValue(data)
                }
            }.onFailure {
                onFailure(this)
            }.onException {
                onError(this)
            }
        }
    }
}