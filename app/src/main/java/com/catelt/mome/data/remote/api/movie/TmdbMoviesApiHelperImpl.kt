package com.catelt.mome.data.remote.api.movie

import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.MoviesResponse
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbMoviesApiHelperImpl @Inject constructor(
    private val tmdbMoviesApi: TmdbMoviesApi
) : TmdbMoviesApiHelper {
    override fun getConfig(): Call<Config> {
        return tmdbMoviesApi.getConfig()
    }

    override suspend fun discoverMovies(
        page: Int,
        isoCode: String,
        region: String,
        sortTypeParam: SortTypeParam,
        genresParam: GenresParam,
        watchProvidersParam: WatchProvidersParam,
        voteRange: ClosedFloatingPointRange<Float>,
        fromReleaseDate: DateParam?,
        toReleaseDate: DateParam?
    ): MoviesResponse {
        return tmdbMoviesApi.discoverMovies(
            page,
            isoCode,
            region,
            sortTypeParam,
            genresParam,
            watchProvidersParam,
            voteAverageMin = voteRange.start,
            voteAverageMax = voteRange.endInclusive,
            fromReleaseDate = fromReleaseDate,
            toReleaseDate = toReleaseDate
        )
    }
}