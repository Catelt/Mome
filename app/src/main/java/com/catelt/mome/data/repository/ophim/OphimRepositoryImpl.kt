package com.catelt.mome.data.repository.ophim

import com.catelt.mome.data.model.ophim.OphimResponse
import com.catelt.mome.data.remote.api.ophim.OphimApiHelper
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OphimRepositoryImpl @Inject constructor(
    private val ophimApiHelper: OphimApiHelper
) : OphimRepository {
    override fun getMediaDetail(slugName: String): Call<OphimResponse> {
        return ophimApiHelper.getMediaDetail(slugName)
    }
}