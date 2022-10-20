package com.catelt.mome.data.model.ophim

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OphimEpisodeResponse(
    @Json(name = "server_name") val serverName: String,
    @Json(name = "server_data") val episodes: List<OphimEpisode>
)
