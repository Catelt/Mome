package com.catelt.mome.data.model.ophim

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OphimResponse(
    val status: Boolean = true,
    @Json(name = "episodes") val episodeResponses: List<OphimEpisodeResponse>
)