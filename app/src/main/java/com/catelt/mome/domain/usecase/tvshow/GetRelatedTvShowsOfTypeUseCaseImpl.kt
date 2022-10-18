package com.catelt.mome.domain.usecase.tvshow

import androidx.paging.PagingData
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.RelationType
import com.catelt.mome.data.model.tvshow.TvShow
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRelatedTvShowsOfTypeUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    operator fun invoke(
        tvShowId: Int,
        type: RelationType,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<TvShow>> {
        return when (type) {
            RelationType.Similar -> {
                tvShowRepository.similarTvShows(
                    tvShowId = tvShowId,
                    deviceLanguage = deviceLanguage
                )
            }
            RelationType.Recommended -> {
                tvShowRepository.tvShowsRecommendations(
                    tvShowId = tvShowId,
                    deviceLanguage = deviceLanguage
                )
            }
        }
    }
}