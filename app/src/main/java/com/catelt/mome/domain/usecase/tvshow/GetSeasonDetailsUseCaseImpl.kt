package com.catelt.mome.domain.usecase.tvshow

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.SeasonDetails
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import javax.inject.Inject

class GetSeasonDetailsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(
        tvShowId: Int,
        seasonNumber: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<SeasonDetails> {
        return tvShowRepository.seasonDetails(
            tvShowId = tvShowId,
            seasonNumber = seasonNumber,
            deviceLanguage = deviceLanguage
        ).awaitApiResponse()
    }
}