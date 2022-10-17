package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import com.catelt.mome.data.model.search.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbOthersApi {
    @GET("configuration")
    fun getConfig(): Call<Config>

    @GET("search/multi")
    suspend fun multiSearch(
        @Query("page") page: Int,
        @Query("language") isoCode: String,
        @Query("region") region: String,
        @Query("query") query: String,
        @Query("year") year: Int?,
        @Query("include_adult") includeAdult: Boolean,
        @Query("primary_release_year") releaseYear: Int?
    ): SearchResponse


    @GET("collection/{collection_id}")
    fun getCollection(
        @Path("collection_id") collectionId: Int,
        @Query("language") isoCode: String
    ): Call<CollectionResponse>
}