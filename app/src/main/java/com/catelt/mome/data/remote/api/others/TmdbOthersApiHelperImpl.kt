package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbOthersApiHelperImpl @Inject constructor(
    private val tmdbOthersApi: TmdbOthersApi
) : TmdbOthersApiHelper {
    override fun getConfig(): Call<Config> {
        return tmdbOthersApi.getConfig()
    }

    override fun getCollection(collectionId: Int, isoCode: String): Call<CollectionResponse> {
        return tmdbOthersApi.getCollection(
            collectionId, isoCode
        )
    }
}