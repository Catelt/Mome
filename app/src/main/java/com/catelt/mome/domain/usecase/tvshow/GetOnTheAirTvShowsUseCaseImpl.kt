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


class GetOnTheAirTvShowsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        deviceLanguage: DeviceLanguage,
        filtered: Boolean
    ): Flow<PagingData<Presentable>> {
        return tvShowRepository.onTheAirTvShows(
            deviceLanguage
        ).mapLatest { data ->
            if (filtered) data.filterCompleteInfo() else data
        }.mapLatest { data -> data.map { it } }
    }

    private fun PagingData<TvShow>.filterCompleteInfo(): PagingData<TvShow> {
        return filter { tvShow ->
            tvShow.run {
                !backdropPath.isNullOrEmpty() &&
                        !posterPath.isNullOrEmpty() &&
                        title.isNotEmpty() &&
                        overview.isNotEmpty()
            }
        }
    }
}