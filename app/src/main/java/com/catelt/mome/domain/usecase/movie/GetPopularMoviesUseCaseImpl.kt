package com.catelt.mome.domain.usecase.movie

import androidx.paging.PagingData
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.repository.movie.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetPopularMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> {
        return movieRepository.popularMovies(
            deviceLanguage
        ).mapLatest { data -> data.map { it } }
    }
}