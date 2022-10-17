package com.example.CateltMovie.data.repository.tvshow

import androidx.paging.PagingData
import com.catelt.mome.data.model.*
import com.catelt.mome.data.model.tvshow.TvShow
import com.catelt.mome.data.model.tvshow.TvShowDetails
import kotlinx.coroutines.flow.Flow
import retrofit2.Call

interface TvShowRepository {
    fun discoverTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default,
        sortType: SortType = SortType.Popularity,
        sortOrder: SortOrder = SortOrder.Desc,
        genresParam: GenresParam = GenresParam(genres = emptyList()),
        watchProvidersParam: WatchProvidersParam = WatchProvidersParam(watchProviders = emptyList()),
        voteRange: ClosedFloatingPointRange<Float> = 0f..10f,
        onlyWithPosters: Boolean = false,
        onlyWithScore: Boolean = false,
        onlyWithOverview: Boolean = false,
        airDateRange: DateRange = DateRange()
    ): Flow<PagingData<TvShow>>

    fun topRatedTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun onTheAirTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun trendingTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun popularTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun airingTodayTvShows(
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun similarTvShows(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun tvShowsRecommendations(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<TvShow>>

    fun getTvShowDetails(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Call<TvShowDetails>

    fun tvShowImages(
        tvShowId: Int,
    ): Call<ImagesResponse>

    fun tvShowVideos(
        tvShowId: Int,
        isoCode: String = DeviceLanguage.default.languageCode
    ): Call<VideosResponse>
}