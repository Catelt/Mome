package com.catelt.mome.data.remote.api.tvshow

import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.tvshow.TvSeasonsResponse
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.model.tvshow.TvShowsResponse
import retrofit2.Call

interface TmdbTvShowsApiHelper {
    fun getConfig(): Call<Config>

    suspend fun discoverTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region,
        sortTypeParam: SortTypeParam = SortTypeParam.PopularityDesc,
        genresParam: GenresParam = GenresParam(genres = emptyList()),
        watchProvidersParam: WatchProvidersParam = WatchProvidersParam(watchProviders = emptyList()),
        voteRange: ClosedFloatingPointRange<Float> = 0f..10f,
        fromAirDate: DateParam? = null,
        toAirDate: DateParam? = null
    ): TvShowsResponse

    suspend fun getTopRatedTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    suspend fun getOnTheAirTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    suspend fun getPopularTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    suspend fun getAiringTodayTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    fun getTvShowDetails(
        tvShowId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<TvShowDetails>

    suspend fun getSimilarTvShows(
        tvShowId: Int,
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    suspend fun getTvShowsRecommendations(
        tvShowId: Int,
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    fun getTvSeasons(
        tvShowId: Int,
        seasonNumber: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<TvSeasonsResponse>

    suspend fun getTrendingTvShows(
        page: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
        region: String = DeviceLanguage.default.region
    ): TvShowsResponse

    fun getSeasonDetails(
        tvShowId: Int,
        seasonNumber: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<SeasonDetails>

    fun getTvShowImages(tvShowId: Int): Call<ImagesResponse>

    fun getEpisodeImages(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Call<ImagesResponse>

    fun getTvShowsGenres(isoCode: String = DeviceLanguage.default.languageCode): Call<GenresResponse>

    fun getTvShowVideos(
        tvShowId: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
    ): Call<VideosResponse>

    fun getSeasonVideos(
        tvShowId: Int,
        seasonNumber: Int,
        isoCode: String = DeviceLanguage.default.languageCode,
    ): Call<VideosResponse>

    fun getSeasonCredits(
        tvShowId: Int,
        seasonNumber: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<AggregatedCredits>
}