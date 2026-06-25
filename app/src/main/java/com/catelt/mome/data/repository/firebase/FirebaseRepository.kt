package com.catelt.mome.data.repository.firebase

import androidx.paging.PagingData
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.firebase.MovieFirebase
import com.catelt.mome.data.model.firebase.TimeAt
import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface FirebaseRepository {
    fun signInWithCredential(credential: AuthCredential): Flow<FirebaseResponse<FirebaseUser>>

    fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<FirebaseResponse<FirebaseUser>>

    fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<FirebaseResponse<FirebaseUser>>

    fun sendPasswordResetEmail(email: String): Flow<FirebaseResponse<String?>>

    fun setDataFirebase(
        collectionId: String,
        documentId: String,
        data: Any
    ): Flow<FirebaseResponse<String?>>

    fun getDataFirebase(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<DocumentSnapshot>>

    fun checkExistedData(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<Boolean>>

    fun deleteDataFirebase(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<String?>>

    fun getMyList(
        userId: String,
    ): Flow<PagingData<Media>>

    fun addMediaMyList(
        userId: String,
        media: Media
    ): Flow<FirebaseResponse<String?>>

    fun removeMediaMyList(
        userId: String,
        mediaId: Int,
    ): Flow<FirebaseResponse<String?>>

    fun checkMediaInMyList(
        userId: String,
        mediaId: Int,
    ): Flow<FirebaseResponse<Boolean>>

    fun addWatchTimeAt(
        userId: String,
        mediaId: Int,
        episode: Int,
        timeAt: TimeAt,
    ): Flow<FirebaseResponse<Boolean>>

    fun getWatchTimeAt(
        userId: String,
        mediaId: Int,
        episode: Int,
    ): Flow<FirebaseResponse<TimeAt>>

    fun getMovie(
        mediaId: Int
    ): Flow<FirebaseResponse<MovieFirebase>>
}