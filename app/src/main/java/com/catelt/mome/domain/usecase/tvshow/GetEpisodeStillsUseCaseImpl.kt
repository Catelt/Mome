package com.catelt.mome.domain.usecase.tvshow

import com.catelt.mome.data.model.Image
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import javax.inject.Inject

class GetEpisodeStillsUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): ApiResponse<List<Image>> {
        val response = tvShowRepository.episodesImage(
            tvShowId = tvShowId, seasonNumber = seasonNumber, episodeNumber = episodeNumber
        ).awaitApiResponse()

        return when (response) {
            is ApiResponse.Success -> {
                val stills = response.data?.stills ?: emptyList()
                ApiResponse.Success(stills)
            }
            is ApiResponse.Failure -> ApiResponse.Failure(response.apiError)
            is ApiResponse.Exception -> ApiResponse.Exception(response.exception)
        }
    }

}