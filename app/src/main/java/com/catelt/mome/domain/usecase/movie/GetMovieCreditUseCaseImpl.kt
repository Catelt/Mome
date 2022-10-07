package com.catelt.mome.domain.usecase.movie

import com.catelt.mome.data.model.Credits
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.catelt.mome.data.repository.movie.MovieRepository
import javax.inject.Inject

class GetMovieCreditUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(
        movieId: Int,
        deviceLanguage: DeviceLanguage
    ): ApiResponse<Credits> {
        return movieRepository.movieCredits(
            movieId = movieId,
            isoCode = deviceLanguage.languageCode
        ).awaitApiResponse()
    }

}