package com.catelt.mome.data.repository.firebase

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
}