package com.catelt.mome.domain.usecase.firebase

import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.model.firebase.TimeAt
import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.catelt.mome.data.repository.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddWatchTimeAtUseCaseImpl @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val userManager: UserManager
) {
    operator fun invoke(mediaId: Int,episode: Int = 1,timeAt: TimeAt): Flow<FirebaseResponse<Boolean>> {
        return firebaseRepository.addWatchTimeAt(userManager.getSessionId(),mediaId,episode,timeAt)
    }
}