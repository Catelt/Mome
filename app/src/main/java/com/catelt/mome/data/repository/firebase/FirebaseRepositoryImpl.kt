package com.catelt.mome.data.repository.firebase

import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepositoryImpl @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val db: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : FirebaseRepository {
    override fun signInWithCredential(credential: AuthCredential): Flow<FirebaseResponse<FirebaseUser>> =
        callbackFlow {
            trySend(FirebaseResponse.Loading)
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        trySend(FirebaseResponse.Success(user))
                        close()
                    }
                }
                .addOnFailureListener {
                    trySend(FirebaseResponse.Error(it))
                }
            awaitClose {
                channel.close()
            }
        }.flowOn(defaultDispatcher)


    override fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<FirebaseResponse<FirebaseUser>> = callbackFlow {
        trySend(FirebaseResponse.Loading)
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = firebaseAuth.currentUser
                user?.let {
                    trySend(FirebaseResponse.Success(user))
                }
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Flow<FirebaseResponse<FirebaseUser>> = callbackFlow {
        trySend(FirebaseResponse.Loading)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = firebaseAuth.currentUser
                user?.let {
                    trySend(FirebaseResponse.Success(user))
                }
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun sendPasswordResetEmail(email: String): Flow<FirebaseResponse<String?>> =
        callbackFlow {
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    trySend(FirebaseResponse.Success(""))
                }
                .addOnFailureListener {
                    trySend(FirebaseResponse.Error(it))
                }
            awaitClose {
                channel.close()
            }
        }.flowOn(defaultDispatcher)

    override fun setDataFirebase(
        collectionId: String,
        documentId: String,
        data: Any
    ): Flow<FirebaseResponse<String?>> = callbackFlow {
        db.collection(collectionId)
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success(""))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun getDataFirebase(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<DocumentSnapshot>> = callbackFlow {
        db.collection(collectionId)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success(it))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun checkExistedData(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<Boolean>> = callbackFlow {
        db.collection(collectionId)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                if (it.exists() && it.data != null) {
                    trySend(FirebaseResponse.Success(true))
                } else {
                    trySend(FirebaseResponse.Success(false))
                }
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun deleteDataFirebase(
        collectionId: String,
        documentId: String
    ): Flow<FirebaseResponse<String?>> = callbackFlow {
        db.collection(collectionId)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success(""))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)
}