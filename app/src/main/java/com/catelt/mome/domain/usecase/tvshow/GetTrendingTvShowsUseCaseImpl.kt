package com.catelt.mome.domain.usecase.tvshow

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.data.model.tvshow.TvShow
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject


class GetTrendingTvShowsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        deviceLanguage: DeviceLanguage,
        genreId: Int = 0,
    ): Flow<PagingData<Presentable>> {
        return tvShowRepository.trendingTvShows(deviceLanguage).mapLatest { data ->
            if (genreId != 0) data.filterCompleteInfo(genreId) else data
        }.mapLatest { data -> data.map { it } }
    }

    private fun PagingData<TvShow>.filterCompleteInfo(genreId: Int): PagingData<TvShow> {
        return filter { movie ->
            movie.genreIds.contains(genreId)
        }
    }
}