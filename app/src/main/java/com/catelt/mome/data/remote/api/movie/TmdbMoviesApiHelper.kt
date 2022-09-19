package com.catelt.mome.data.remote.api.movie

import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.MoviesResponse
import retrofit2.Call

interface TmdbMoviesApiHelper {
    fun getConfig(): Call<Config>

    suspend fun discoverMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
        sortTypeParam: SortTypeParam = SortTypeParam.PopularityDesc,
        genresParam: GenresParam = GenresParam(genres = emptyList()),
        watchProvidersParam: WatchProvidersParam = WatchProvidersParam(watchProviders = emptyList()),
        voteRange: ClosedFloatingPointRange<Float> = 0f..10f,
        fromReleaseDate: DateParam? = null,
        toReleaseDate: DateParam? = null
    ): MoviesResponse

}