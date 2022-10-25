package com.catelt.mome.data.model.tvshow

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TvShowParcelable(
    val numberEpisode: Int,
    val stillPath: String?,
    val title: String?,
    val url: String?,
    val overview: String?
) : Parcelable