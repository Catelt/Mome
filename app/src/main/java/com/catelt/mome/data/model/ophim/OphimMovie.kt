package com.catelt.mome.data.model.ophim

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OphimMovie(
    val slug: String,
    val name: String,
    @Json(name= "origin_name") val originName: String,
)
