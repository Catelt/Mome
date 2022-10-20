package com.catelt.mome.data.model.ophim

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OphimEpisode(
    val name: String,
    val slug: String,
    val filename: String,
    @Json(name = "link_embed") val embed: String,
    @Json(name = "link_m3u8") val url: String
)
