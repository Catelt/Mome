package com.catelt.mome.data.remote.api.movie

import androidx.annotation.FloatRange
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.MoviesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbMoviesApi {
    @GET("configuration")
    fun getConfig(): Call<Config>

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int,
        @Query("language") isoCode: String,
        @Query("region") region: String,
        @Query("sort_by") type: SortTypeParam,
        @Query("with_genres") genres: GenresParam,
        @Query("with_watch_providers") watchProviders: WatchProvidersParam,
        @Query("watch_region") watchRegion: String = region,
        @FloatRange(from = 0.0)
        @Query("vote_average.gte")
        voteAverageMin: Float,
        @FloatRange(from = 0.0)
        @Query("vote_average.lte")
        voteAverageMax: Float,
        @Query("release_date.gte")
        fromReleaseDate: DateParam?,
        @Query("release_date.lte")
        toReleaseDate: DateParam?
    ): MoviesResponse
}