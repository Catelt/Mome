package com.catelt.mome.domain.usecase.tvshow

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import javax.inject.Inject

class GetTvShowVideosUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(
        tvShowId: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<List<Video>> {
        val response = tvShowRepository.tvShowVideos(
            tvShowId = tvShowId,
            isoCode = deviceLanguage.languageCode
        ).awaitApiResponse()

        return when (response) {
            is ApiResponse.Success -> {
                val videos = response.data?.results?.sortedWith(
                    compareBy(
                        { video -> video.official },
                        { video -> video.publishedAt }
                    )
                ) ?: emptyList()
                ApiResponse.Success(videos)
            }
            is ApiResponse.Failure -> ApiResponse.Failure(response.apiError)
            is ApiResponse.Exception -> ApiResponse.Exception(response.exception)
        }
    }
}