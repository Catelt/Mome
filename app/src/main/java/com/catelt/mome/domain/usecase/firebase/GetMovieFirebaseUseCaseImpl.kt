package com.catelt.mome.domain.usecase.firebase

import com.catelt.mome.data.model.firebase.MovieFirebase
import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.catelt.mome.data.repository.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieFirebaseUseCaseImpl @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
) {
    operator fun invoke(mediaId: Int): Flow<FirebaseResponse<MovieFirebase>> {
        return firebaseRepository.getMovie(mediaId)
    }
}