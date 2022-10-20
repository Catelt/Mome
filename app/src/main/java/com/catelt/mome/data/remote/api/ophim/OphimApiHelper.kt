package com.catelt.mome.data.remote.api.ophim

import com.catelt.mome.data.model.ophim.OphimResponse
import retrofit2.Call

interface OphimApiHelper {
    fun getMediaDetail(
        slugName: String
    ): Call<OphimResponse>
}