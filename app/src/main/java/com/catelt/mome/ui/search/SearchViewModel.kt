package com.catelt.mome.ui.search

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.DetailPresentable
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.repository.config.ConfigRepository
import com.catelt.mome.domain.usecase.GetDeviceLanguageUseCaseImpl
import com.catelt.mome.domain.usecase.GetMediaMultiSearchUseCaseImpl
import com.catelt.mome.domain.usecase.GetSpeechToTextAvailableUseCaseImpl
import com.catelt.mome.domain.usecase.movie.GetPopularMoviesUseCaseImpl
import com.catelt.mome.utils.ImageUrlParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getDeviceLanguageUseCase: GetDeviceLanguageUseCaseImpl,
    private val getMediaMultiSearchUseCase: GetMediaMultiSearchUseCaseImpl,
    private val getPopularMoviesUseCase: GetPopularMoviesUseCaseImpl,
    private val getSpeechToTextAvailableUseCase: GetSpeechToTextAvailableUseCaseImpl,
    private val configRepository: ConfigRepository
) : BaseViewModel() {
    private val deviceLanguage: Flow<DeviceLanguage> = getDeviceLanguageUseCase()
    private val voiceSearchAvailable: Flow<Boolean> = getSpeechToTextAvailableUseCase()

    val imageUrlParser: StateFlow<ImageUrlParser?> = configRepository.getImageUrlParser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val queryDelay = 500.milliseconds

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val popularMovies: Flow<PagingData<DetailPresentable>> =
        deviceLanguage.mapLatest { deviceLanguage ->
            getPopularMoviesUseCase(deviceLanguage)
        }.flattenMerge().cachedIn(viewModelScope)

    private val queryState: MutableStateFlow<QueryState> = MutableStateFlow(QueryState.default)
    private val searchState: MutableStateFlow<SearchState> =
        MutableStateFlow(SearchState.EmptyQuery)
    private val resultState: MutableStateFlow<ResultState> =
        MutableStateFlow(ResultState.Default(popularMovies))
    private val queryLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<SearchScreenUIState> = combine(
        queryState, searchState, resultState, voiceSearchAvailable
    ) { queryState, searchState, resultState, voiceSearch ->
        SearchScreenUIState(
            query = queryState.query,
            searchState = searchState,
            resultState = resultState,
            queryLoading = queryState.loading,
            voiceSearch = voiceSearch
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchScreenUIState.default)

    private var queryJob: Job? = null

    fun onQueryClear() {
        onQueryChange("")
    }

    fun onQueryChange(queryText: String) {
        viewModelScope.launch {
            queryState.emit(queryState.value.copy(query = queryText))

            queryJob?.cancel()

            when {
                queryText.isBlank() -> {
                    searchState.emit(SearchState.EmptyQuery)
                    resultState.emit(ResultState.Default(popularMovies))
                }
                else -> {
                    queryJob = createQueryJob(queryText).apply {
                        start()
                    }
                }
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun createQueryJob(query: String): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(queryDelay)

                queryLoading.emit(true)

                val searchResults = deviceLanguage.mapLatest { deviceLanguage ->
                    getMediaMultiSearchUseCase(
                        query = query,
                        deviceLanguage = deviceLanguage
                    )
                }.flattenMerge().cachedIn(viewModelScope)

                searchState.emit(SearchState.ValidQuery)
                resultState.emit(ResultState.Search(searchResults))
            } catch (_: CancellationException) {

            } finally {
                withContext(NonCancellable) {
                    queryLoading.emit(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        queryJob?.cancel()
    }
}