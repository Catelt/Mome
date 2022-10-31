package com.catelt.mome.ui.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.Genre
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.utils.BUNDLE_ID_GENRE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ItemPickerViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    val genreId = savedStateHandle.getLiveData<Int>(BUNDLE_ID_GENRE)

    val moviesGenres: StateFlow<List<Genre>?> = configRepository.getMoviesGenres()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}