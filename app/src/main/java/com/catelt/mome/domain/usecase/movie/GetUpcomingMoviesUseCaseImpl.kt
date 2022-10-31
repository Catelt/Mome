package com.catelt.mome.domain.usecase.movie

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.repository.movie.MovieRepository
import com.catelt.mome.utils.DateFormat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.time.LocalDate
import javax.inject.Inject

class GetUpcomingMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(deviceLanguage: DeviceLanguage): Flow<PagingData<Movie>> {
        return movieRepository.upcomingMovies(deviceLanguage)
            .mapLatest { data ->
                data.filter {
                    val today = LocalDate.now()
                    val localDate = LocalDate.parse(it.releaseDate, DateFormat.default)
                    localDate.isAfter(today) && it.overview.isNotBlank()
                }.map { it }
            }
    }
}