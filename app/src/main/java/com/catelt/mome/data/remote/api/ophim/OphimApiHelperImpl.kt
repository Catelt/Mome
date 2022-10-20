package com.catelt.mome.data.remote.api.ophim

import com.catelt.mome.data.model.ophim.OphimResponse
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OphimApiHelperImpl @Inject constructor(
    private val ophimApi: OphimApi
) : OphimApiHelper {
    override fun getMediaDetail(slugName: String): Call<OphimResponse> {
        return ophimApi.getMediaDetail(slugName)
    }
}