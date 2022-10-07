package com.catelt.mome.domain.usecase.movie

import androidx.paging.PagingData
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.RelationType
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.repository.movie.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRelatedMoviesOfTypeUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(
        movieId: Int,
        type: RelationType,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<Movie>> {
        return when (type) {
            RelationType.Similar -> {
                movieRepository.similarMovies(
                    movieId = movieId,
                    deviceLanguage = deviceLanguage
                )
            }
            RelationType.Recommended -> {
                movieRepository.moviesRecommendation(
                    movieId = movieId,
                    deviceLanguage = deviceLanguage
                )
            }
        }
    }

}