package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.search.SearchResponse
import retrofit2.Call

interface TmdbOthersApiHelper {
    fun getConfig(): Call<Config>

    suspend fun multiSearch(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
        query: String,
        includeAdult: Boolean = false,
        year: Int? = null,
        releaseYear: Int? = null
    ): SearchResponse

    fun getCollection(
        collectionId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<CollectionResponse>
}