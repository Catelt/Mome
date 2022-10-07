package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import com.catelt.mome.data.model.DeviceLanguage
import retrofit2.Call

interface TmdbOthersApiHelper {
    fun getConfig(): Call<Config>

    fun getCollection(
        collectionId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<CollectionResponse>
}