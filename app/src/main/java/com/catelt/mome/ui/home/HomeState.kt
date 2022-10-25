package com.catelt.mome.ui.home

import androidx.paging.PagingData
import com.catelt.mome.data.model.DetailPresentable
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.data.model.ophim.OphimResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class HomeUIState(
    val homeState: HomeState,
    val ophim: OphimResponse?
) {
    companion object {
        val default: HomeUIState = HomeUIState(
            homeState = HomeState.MovieData(),
            ophim = null
        )
    }
}


data class MoviesState(
    val discover: Flow<PagingData<Presentable>>,
    val popular: Flow<PagingData<DetailPresentable>>,
    val topRated: Flow<PagingData<Presentable>>,
    val trending: Flow<PagingData<Presentable>>,
    val nowPlaying: Flow<PagingData<Presentable>>
) {
    companion object {
        val default: MoviesState = MoviesState(
            discover = emptyFlow(),
            popular = emptyFlow(),
            topRated = emptyFlow(),
            trending = emptyFlow(),
            nowPlaying = emptyFlow()
        )
    }
}

data class TvShowsState(
    val onTheAir: Flow<PagingData<Presentable>>,
    val discover: Flow<PagingData<Presentable>>,
    val topRated: Flow<PagingData<Presentable>>,
    val trending: Flow<PagingData<Presentable>>,
    val airingToday: Flow<PagingData<Presentable>>
) {
    companion object {
        val default: TvShowsState = TvShowsState(
            onTheAir = emptyFlow(),
            discover = emptyFlow(),
            topRated = emptyFlow(),
            trending = emptyFlow(),
            airingToday = emptyFlow()
        )
    }
}


sealed class HomeState {
    data class MovieData(val moviesState: MoviesState = MoviesState.default) : HomeState()
    data class TvShowData(val tvShowsState: TvShowsState) : HomeState()
}