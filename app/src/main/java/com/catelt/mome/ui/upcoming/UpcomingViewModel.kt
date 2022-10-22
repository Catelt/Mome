package com.catelt.mome.ui.upcoming

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.movie.GetUpcomingMoviesUseCaseImpl
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UpcomingViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getUpcomingMoviesUseCase: GetUpcomingMoviesUseCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase.invoke()

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val moviesGenres: StateFlow<List<Genre>?> = configRepository.getMoviesGenres()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val newHotState: StateFlow<Upcoming> = deviceLanguage.mapLatest { deviceLanguage ->
        Upcoming(
            upcoming = getUpcomingMoviesUseCase(deviceLanguage).cachedIn(viewModelScope),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10), Upcoming.default)


    val uiState: StateFlow<UpcomingUIState> = newHotState.mapLatest {
        UpcomingUIState(
            newHotState = it,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UpcomingUIState.default)
}