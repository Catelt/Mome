package com.catelt.mome.domain.usecase.firebase

import androidx.paging.PagingData
import androidx.paging.map
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.repository.firebase.FirebaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class GetMediaMyListUseCaseImpl @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val userManager: UserManager
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PagingData<Media>> {
        return firebaseRepository.getMyList(userManager.getSessionId())
            .mapLatest { data -> data.map { it } }
    }
}