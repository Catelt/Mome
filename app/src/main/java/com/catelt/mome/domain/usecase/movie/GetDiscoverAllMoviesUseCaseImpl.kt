package com.catelt.mome.domain.usecase.movie

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.Presentable
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.repository.movie.MovieRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetDiscoverAllMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        deviceLanguage: DeviceLanguage,
        genreId: Int = 0,
    ): Flow<PagingData<Presentable>> {
        return movieRepository.discoverMovies(deviceLanguage).mapLatest { data ->
            if (genreId != 0) data.filterCompleteInfo(genreId) else data
            }.mapLatest { data -> data.map { it } }
    }

    private fun PagingData<Movie>.filterCompleteInfo(genreId: Int): PagingData<Movie> {
        return filter { movie ->
            movie.genreIds.contains(genreId)
        }
    }
}