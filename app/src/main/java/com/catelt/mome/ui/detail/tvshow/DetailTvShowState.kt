package com.catelt.mome.ui.detail.tvshow

import androidx.paging.PagingData
import com.catelt.mome.data.model.AggregatedCredits
import com.catelt.mome.data.model.Image
import com.catelt.mome.data.model.SeasonDetails
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.model.ophim.OphimResponse
import com.catelt.mome.data.model.tvshow.TvShow
import com.catelt.mome.data.model.tvshow.TvShowDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class DetailTvShowUIState(
    val tvShowDetails: TvShowDetails?,
    val associatedTvShow: AssociatedTvShow,
    val associatedContentTvShow: AssociatedContentTvShow,
    val ophim: OphimResponse?,
    val error: String?
) {
    companion object {
        fun getDefault(): DetailTvShowUIState {
            return DetailTvShowUIState(
                tvShowDetails = null,
                associatedTvShow = AssociatedTvShow.default,
                associatedContentTvShow = AssociatedContentTvShow.default,
                ophim = null,
                error = null
            )
        }
    }
}

data class AssociatedContentTvShow(
    val backdrops: List<Image>,
    val videos: List<Video>?,
    val credits: AggregatedCredits?,
) {
    companion object {
        val default: AssociatedContentTvShow = AssociatedContentTvShow(
            backdrops = emptyList(),
            videos = null,
            credits = null,
        )
    }
}


data class AssociatedTvShow(
    val similar: Flow<PagingData<TvShow>>,
    val recommendations: Flow<PagingData<TvShow>>,
    val seasonDetails: SeasonDetails?,
) {
    companion object {
        val default: AssociatedTvShow = AssociatedTvShow(
            similar = emptyFlow(),
            recommendations = emptyFlow(),
            seasonDetails = null,
        )
    }
}