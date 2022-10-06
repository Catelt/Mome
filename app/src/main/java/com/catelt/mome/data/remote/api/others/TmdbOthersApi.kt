package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbOthersApi {
    @GET("configuration")
    fun getConfig(): Call<Config>

    @GET("collection/{collection_id}")
    fun getCollection(
        @Path("collection_id") collectionId: Int,
        @Query("language") isoCode: String
    ): Call<CollectionResponse>
}