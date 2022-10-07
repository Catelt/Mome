package com.catelt.mome.domain.usecase.movie

import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Video
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.catelt.mome.data.repository.movie.MovieRepository
import javax.inject.Inject


class GetMovieVideosUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(
        movieId: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<List<Video>> {
        val response = movieRepository.getMovieVideos(
            movieId = movieId,
            isoCode = deviceLanguage.languageCode
        ).awaitApiResponse()

        return when (response) {
            is ApiResponse.Success -> {
                val results = response.data?.results
                val videos = results?.sortedWith(
                    compareBy<Video> { video ->
                        video.language == deviceLanguage.languageCode
                    }.thenByDescending { video ->
                        video.publishedAt
                    }
                )
                ApiResponse.Success(videos)
            }
            is ApiResponse.Failure -> ApiResponse.Failure(response.apiError)
            is ApiResponse.Exception -> ApiResponse.Exception(response.exception)
        }

    }

}