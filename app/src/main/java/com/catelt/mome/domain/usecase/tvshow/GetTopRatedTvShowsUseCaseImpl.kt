package com.catelt.mome.domain.usecase.tvshow

import androidx.paging.PagingData
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Presentable
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject


class GetTopRatedTvShowsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(deviceLanguage: DeviceLanguage): Flow<PagingData<Presentable>> {
        return tvShowRepository.topRatedTvShows(deviceLanguage)
            .mapLatest { data -> data.map { it } }
    }
}