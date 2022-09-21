package com.catelt.mome.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImagesResponse(
    val id: Int,
    val backdrops: List<Image>?,
    val stills: List<Image>?
)