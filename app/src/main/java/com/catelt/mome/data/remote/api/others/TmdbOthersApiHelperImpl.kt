package com.catelt.mome.data.remote.api.others

import com.catelt.mome.data.model.CollectionResponse
import com.catelt.mome.data.model.Config
import com.catelt.mome.data.model.search.SearchResponse
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

    override suspend fun multiSearch(
        page: Int,
        isoCode: String,
        region: String,
        query: String,
        includeAdult: Boolean,
        year: Int?,
        releaseYear: Int?
    ): SearchResponse {
        return tmdbOthersApi.multiSearch(
            page = page,
            isoCode = isoCode,
            region = region,
            query = query,
            includeAdult = includeAdult,
            year = year,
            releaseYear = releaseYear
        )
    }

    override fun getCollection(collectionId: Int, isoCode: String): Call<CollectionResponse> {
        return tmdbOthersApi.getCollection(
            collectionId, isoCode
        )
    }
}