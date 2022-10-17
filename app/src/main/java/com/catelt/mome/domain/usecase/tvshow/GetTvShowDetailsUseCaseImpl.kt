package com.catelt.mome.domain.usecase.tvshow

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.tvshow.TvShowDetails
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import javax.inject.Inject


class GetTvShowDetailsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<TvShowDetails> {
        return tvShowRepository.getTvShowDetails(
            tvShowId = tvShowId,
            deviceLanguage = deviceLanguage
        ).awaitApiResponse()
    }

}