package com.catelt.mome.data.remote.api.ophim

import com.catelt.mome.data.model.ophim.OphimResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface OphimApi {
    @GET("phim/{slug_name}")
    fun getMediaDetail(
        @Path("slug_name") slugName: String
    ): Call<OphimResponse>
}