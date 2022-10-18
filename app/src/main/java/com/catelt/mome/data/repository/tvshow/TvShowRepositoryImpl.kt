package com.example.CateltMovie.data.repository.tvshow

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.tvshow.TvShow
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.paging.tvshow.DiscoverTvShowsPagingDataSource
import com.catelt.mome.data.paging.tvshow.TvShowDetailsResponsePagingDataSource
import com.catelt.mome.data.paging.tvshow.TvShowResponsePagingDataSource
import com.catelt.mome.data.remote.api.tvshow.TmdbTvShowsApiHelper
import com.catelt.mome.utils.SIZE_PAGE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalPagingApi::class)
class TvShowRepositoryImpl @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val apiTvShowHelper: TmdbTvShowsApiHelper,
) : TvShowRepository {
    override fun discoverTvShows(
        deviceLanguage: DeviceLanguage,
        sortType: SortType,
        sortOrder: SortOrder,
        genresParam: GenresParam,
        watchProvidersParam: WatchProvidersParam,
        voteRange: ClosedFloatingPointRange<Float>,
        onlyWithPosters: Boolean,
        onlyWithScore: Boolean,
        onlyWithOverview: Boolean,
        airDateRange: DateRange
    ): Flow<PagingData<TvShow>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        DiscoverTvShowsPagingDataSource(
            apiTvShowsHelper = apiTvShowHelper,
            deviceLanguage = deviceLanguage,
            sortType = sortType,
            sortOrder = sortOrder,
            genresParam = genresParam,
            watchProvidersParam = watchProvidersParam,
            voteRange = voteRange,
            onlyWithPosters = onlyWithPosters,
            onlyWithScore = onlyWithScore,
            onlyWithOverview = onlyWithOverview,
            airDateRange = airDateRange
        )
    }.flow.flowOn(defaultDispatcher)

    override fun topRatedTvShows(deviceLanguage: DeviceLanguage): Flow<PagingData<TvShow>> =
        Pager(
            PagingConfig(pageSize = SIZE_PAGE)
        ){
            TvShowResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiHelperMethod = apiTvShowHelper::getOnTheAirTvShows
            )
        }.flow.flowOn(defaultDispatcher)

    override fun onTheAirTvShows(deviceLanguage: DeviceLanguage): Flow<PagingData<TvShow>> =
        Pager(
            PagingConfig(pageSize = SIZE_PAGE)
        ){
            TvShowResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiHelperMethod = apiTvShowHelper::getOnTheAirTvShows
            )
        }.flow.flowOn(defaultDispatcher)

    override fun trendingTvShows(deviceLanguage: DeviceLanguage): Flow<PagingData<TvShow>> =
        Pager(
            PagingConfig(pageSize = SIZE_PAGE)
        ){
            TvShowResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiHelperMethod = apiTvShowHelper::getTrendingTvShows
            )
        }.flow.flowOn(defaultDispatcher)

    override fun popularTvShows(deviceLanguage: DeviceLanguage): Flow<PagingData<TvShow>> =
        Pager(
            PagingConfig(pageSize = SIZE_PAGE)
        ){
            TvShowResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiHelperMethod = apiTvShowHelper::getPopularTvShows
            )
        }.flow.flowOn(defaultDispatcher)

    override fun airingTodayTvShows(deviceLanguage: DeviceLanguage): Flow<PagingData<TvShow>> =
        Pager(
            PagingConfig(pageSize = SIZE_PAGE)
        ){
            TvShowResponsePagingDataSource(
                language = deviceLanguage.languageCode,
                apiHelperMethod = apiTvShowHelper::getAiringTodayTvShows
            )
        }.flow.flowOn(defaultDispatcher)

    override fun similarTvShows(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<TvShow>> = Pager(
        PagingConfig(pageSize = SIZE_PAGE)
    ) {
        TvShowDetailsResponsePagingDataSource(
            tvShowId = tvShowId,
            deviceLanguage = deviceLanguage,
            apiHelperMethod = apiTvShowHelper::getSimilarTvShows
        )
    }.flow.flowOn(defaultDispatcher)

    override fun tvShowsRecommendations(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<TvShow>> = Pager(
        PagingConfig(pageSize = SIZE_PAGE)
    ) {
        TvShowDetailsResponsePagingDataSource(
            tvShowId = tvShowId,
            deviceLanguage = deviceLanguage,
            apiHelperMethod = apiTvShowHelper::getTvShowsRecommendations
        )
    }.flow.flowOn(defaultDispatcher)

    override fun getTvShowDetails(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage
    ): Call<TvShowDetails> {
        return apiTvShowHelper.getTvShowDetails(tvShowId, deviceLanguage.languageCode)
    }

    override fun tvShowImages(tvShowId: Int): Call<ImagesResponse> {
        return apiTvShowHelper.getTvShowImages(tvShowId)
    }

    override fun tvShowVideos(tvShowId: Int, isoCode: String): Call<VideosResponse> {
        return apiTvShowHelper.getTvShowVideos(tvShowId, isoCode)
    }

    override fun seasonDetails(
        tvShowId: Int,
        seasonNumber: Int,
        deviceLanguage: DeviceLanguage
    ): Call<SeasonDetails> {
        return apiTvShowHelper.getSeasonDetails(tvShowId, seasonNumber, deviceLanguage.languageCode)
    }

    override fun episodesImage(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Call<ImagesResponse> {
        return apiTvShowHelper.getEpisodeImages(tvShowId, seasonNumber, episodeNumber)
    }

    override fun seasonCredits(
        tvShowId: Int,
        seasonNumber: Int,
        isoCode: String
    ): Call<AggregatedCredits> {
        return apiTvShowHelper.getSeasonCredits(tvShowId, seasonNumber, isoCode)
    }
}