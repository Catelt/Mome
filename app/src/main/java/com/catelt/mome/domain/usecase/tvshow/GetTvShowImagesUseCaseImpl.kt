package com.catelt.mome.domain.usecase.tvshow

import com.catelt.mome.data.model.Image
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.example.CateltMovie.data.repository.tvshow.TvShowRepository
import javax.inject.Inject

class GetTvShowImagesUseCaseImpl @Inject constructor(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(tvShowId: Int): ApiResponse<List<Image>> {
        val response = tvShowRepository.tvShowImages(tvShowId).awaitApiResponse()

        return when (response) {
            is ApiResponse.Success -> {
                val images = response.data?.backdrops ?: emptyList()
                ApiResponse.Success(images)
            }
            is ApiResponse.Failure -> ApiResponse.Failure(response.apiError)
            is ApiResponse.Exception -> ApiResponse.Exception(response.exception)
        }
    }
}