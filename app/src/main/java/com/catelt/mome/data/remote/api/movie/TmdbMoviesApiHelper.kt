package com.catelt.mome.data.remote.api.movie

import com.catelt.mome.data.VideosResponse
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.MovieDetails
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

    suspend fun getPopularMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
    ): MoviesResponse

    suspend fun getUpcomingMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
    ): MoviesResponse

    suspend fun getTopRatedMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
    ): MoviesResponse

    suspend fun getNowPlayingMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): MoviesResponse

    fun getMovieDetails(
        movieId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<MovieDetails>

    fun getMovieCredits(
        movieId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<Credits>

    suspend fun getSimilarMovies(
        movieId: Int,
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): MoviesResponse

    suspend fun getMoviesRecommendations(
        movieId: Int,
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): MoviesResponse

    suspend fun getTrendingMovies(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): MoviesResponse

    fun getMovieImages(movieId: Int): Call<ImagesResponse>

    fun getMoviesGenres(isoCode: String = DeviceLanguage.default.languageCode): Call<GenresResponse>

    fun getMovieWatchProviders(
        movieId: Int
    ): Call<WatchProvidersResponse>

    fun getAllMoviesWatchProviders(
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): Call<AllWatchProvidersResponse>

    fun getMovieVideos(
        movieId: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
    ): Call<VideosResponse>

    suspend fun getOtherMoviesOfDirector(
        page: Int,
        isoCode: String,
        region: String,
        directorId: Int
    ): MoviesResponse
}