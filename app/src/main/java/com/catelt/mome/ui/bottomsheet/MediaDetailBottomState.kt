package com.catelt.mome.ui.bottomsheet

import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.model.ophim.OphimMovie
import com.catelt.mome.data.model.tvshow.TvShowDetails

data class MediaDetailBottomState(
    val movieDetails: MovieDetails?,
    val tvShowsDetails: TvShowDetails?,
    val isMyList: Boolean,
    val ophim: OphimMovie?
) {
    companion object {
        fun getDefault(): MediaDetailBottomState {
            return MediaDetailBottomState(
                movieDetails = null,
                tvShowsDetails = null,
                isMyList = false,
                ophim = null
            )
        }
    }
}