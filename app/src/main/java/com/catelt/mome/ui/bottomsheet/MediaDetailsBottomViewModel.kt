package com.catelt.mome.ui.bottomsheet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DetailPresentable
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.MediaType
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.remote.api.onException
import com.catelt.mome.data.remote.api.onFailure
import com.catelt.mome.data.remote.api.onSuccess
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.AddMediaMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.CheckMediaInMyListUseCaseImpl
import com.catelt.mome.domain.usecase.firebase.RemoveMediaMyListUseCaseImpl
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
    private val addMediaMyListUseCase: AddMediaMyListUseCaseImpl,
    private val removeMediaMyListUseCase: RemoveMediaMyListUseCaseImpl,
    private val checkMediaInMyListUseCase: CheckMediaInMyListUseCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()
    val isMyList = MutableStateFlow(false)

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

    suspend fun checkMediaInMyList(presentable: DetailPresentable) {
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