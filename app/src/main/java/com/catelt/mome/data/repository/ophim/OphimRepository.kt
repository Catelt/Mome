package com.catelt.mome.data.repository.ophim

import com.catelt.mome.data.model.ophim.OphimResponse
import retrofit2.Call

interface OphimRepository {
    fun getMediaDetail(slugName: String): Call<OphimResponse>
}