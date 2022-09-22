package com.catelt.mome.data.repository.movie

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.model.movie.MovieDetails
import com.catelt.mome.data.paging.movie.DirectorOtherMoviePagingDataSource
import com.catelt.mome.data.paging.movie.DiscoverMoviesPagingDataSource
import com.catelt.mome.data.paging.movie.MovieDetailsResponsePagingDataSource
import com.catelt.mome.data.paging.movie.MovieResponsePagingDataSource
import com.catelt.mome.data.remote.api.movie.TmdbMoviesApiHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalPagingApi::class)
class MovieRepositoryImpl @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiMovieHelper: TmdbMoviesApiHelper,
) : MovieRepository {
    override fun discoverMovies(
        deviceLanguage: DeviceLanguage,
        sortType: SortType,
        sortOrder: SortOrder,
        genresParam: GenresParam,
        watchProvidersParam: WatchProvidersParam,
        voteRange: ClosedFloatingPointRange<Float>,
        onlyWithPosters: Boolean,
        onlyWithScore: Boolean,
        onlyWithOverview: Boolean,
        releaseDateRange: DateRange
    ): Flow<PagingData<Movie>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        DiscoverMoviesPagingDataSource(
            apiMovieHelper = apiMovieHelper,
            deviceLanguage = deviceLanguage,
            sortType = sortType,
            sortOrder = sortOrder,
            genresParam = genresParam,
            watchProvidersParam = watchProvidersParam,
            voteRange = voteRange,
            onlyWithPosters = onlyWithPosters,
            onlyWithScore = onlyWithScore,
            onlyWithOverview = onlyWithOverview,
            releaseDateRange = releaseDateRange
        )
    }.flow.flowOn(defaultDispatcher)

    override fun popularMovies(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> =
        Pager(
            PagingConfig(pageSize = 20)
        ){
            MovieResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiMovieHelperMethod = apiMovieHelper::getPopularMovies
            )
        }.flow.flowOn(defaultDispatcher)

    override fun upcomingMovies(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> =
        Pager(
            PagingConfig(pageSize = 20)
        ){
            MovieResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiMovieHelperMethod = apiMovieHelper::getUpcomingMovies
            )
        }.flow.flowOn(defaultDispatcher)

    override fun trendingMovies(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> =
        Pager(
            PagingConfig(pageSize = 20)
        ){
            MovieResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiMovieHelperMethod = apiMovieHelper::getTrendingMovies
            )
        }.flow.flowOn(defaultDispatcher)

    override fun topRatedMovies(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> =
        Pager(
            PagingConfig(pageSize = 20)
        ){
            MovieResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiMovieHelperMethod = apiMovieHelper::getTrendingMovies
            )
        }.flow.flowOn(defaultDispatcher)


    override fun nowPlayingMovies(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> =
        Pager(
            PagingConfig(pageSize = 20)
        ){
            MovieResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiMovieHelperMethod = apiMovieHelper::getTrendingMovies
            )
        }.flow.flowOn(defaultDispatcher)

    override fun similarMovies(
        movieId: Int,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<Movie>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        MovieDetailsResponsePagingDataSource(
            movieId = movieId,
            language = deviceLanguage.languageCode,
            apiHMovieHelperMethod = apiMovieHelper::getSimilarMovies
        )
    }.flow.flowOn(defaultDispatcher)

    override fun moviesRecommendation(
        movieId: Int,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<Movie>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        MovieDetailsResponsePagingDataSource(
            movieId = movieId,
            language = deviceLanguage.languageCode,
            apiHMovieHelperMethod = apiMovieHelper::getMoviesRecommendations
        )
    }.flow.flowOn(defaultDispatcher)

    override fun movieDetails(movieId: Int, isoCode: String): Call<MovieDetails> {
        return apiMovieHelper.getMovieDetails(movieId, isoCode)
    }

    override fun movieCredits(movieId: Int, isoCode: String): Call<Credits> {
        return apiMovieHelper.getMovieCredits(movieId, isoCode)
    }

    override fun movieImages(movieId: Int): Call<ImagesResponse> {
        return apiMovieHelper.getMovieImages(movieId)
    }

    override fun watchProviders(movieId: Int): Call<WatchProvidersResponse> {
        return apiMovieHelper.getMovieWatchProviders(movieId)
    }

    override fun getMovieVideos(movieId: Int, isoCode: String): Call<VideosResponse> {
        return apiMovieHelper.getMovieVideos(movieId)
    }

    override fun moviesOfDirector(
        directorId: Int,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<Movie>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        DirectorOtherMoviePagingDataSource(
            apiMovieHelper = apiMovieHelper,
            language = deviceLanguage.languageCode,
            region = deviceLanguage.region,
            directorId = directorId
        )
    }.flow.flowOn(defaultDispatcher)
}