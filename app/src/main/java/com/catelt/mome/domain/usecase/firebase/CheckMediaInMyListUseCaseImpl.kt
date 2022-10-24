package com.catelt.mome.domain.usecase.firebase

import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.catelt.mome.data.repository.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckMediaInMyListUseCaseImpl  @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val userManager: UserManager
) {
    operator fun invoke(mediaId: Int): Flow<FirebaseResponse<Boolean>> {
        return firebaseRepository.checkMediaInMyList(userManager.getSessionId(),mediaId)
    }
}