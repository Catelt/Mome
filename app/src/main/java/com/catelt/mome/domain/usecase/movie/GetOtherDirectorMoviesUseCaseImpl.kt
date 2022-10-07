package com.catelt.mome.domain.usecase.movie

import androidx.paging.PagingData
import com.catelt.mome.data.model.CrewMember
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.movie.Movie
import com.catelt.mome.data.repository.movie.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtherDirectorMoviesUseCaseImpl @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(
        mainDirector: CrewMember,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<Movie>> {
        return movieRepository.moviesOfDirector(
            directorId = mainDirector.id,
            deviceLanguage = deviceLanguage
        )
    }
}