package com.catelt.mome.data.repository.firebase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.catelt.mome.data.model.account.Media
import com.catelt.mome.data.model.firebase.MovieFirebase
import com.catelt.mome.data.model.firebase.TimeAt
import com.catelt.mome.data.paging.firebase.FirebasePagingDataSource
import com.catelt.mome.data.remote.firebase.FirebaseResponse
import com.catelt.mome.utils.*
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
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

    override fun getMyList(userId: String): Flow<PagingData<Media>> = Pager(
        PagingConfig(pageSize = SIZE_PAGE)
    ) {
        FirebasePagingDataSource(
            query = db.collection(USER_FIREBASE).document(userId).collection(MY_LIST_FIREBASE)
                .orderBy(FIELD_ID_MEDIA),
        )
    }.flow.flowOn(defaultDispatcher)

    override fun addMediaMyList(
        userId: String,
        media: Media
    ): Flow<FirebaseResponse<String?>> = callbackFlow {
        db.collection(USER_FIREBASE)
            .document(userId)
            .collection(MY_LIST_FIREBASE)
            .document(media.id.toString())
            .set(media)
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success("Added to My List"))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun removeMediaMyList(
        userId: String,
        mediaId: Int
    ): Flow<FirebaseResponse<String?>> = callbackFlow {
        db.collection(USER_FIREBASE)
            .document(userId)
            .collection(MY_LIST_FIREBASE)
            .document(mediaId.toString())
            .delete()
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success("Remove from My List"))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun checkMediaInMyList(
        userId: String,
        mediaId: Int
    ): Flow<FirebaseResponse<Boolean>> = callbackFlow {
        db.collection(USER_FIREBASE)
            .document(userId)
            .collection(MY_LIST_FIREBASE)
            .document(mediaId.toString())
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

    override fun addWatchTimeAt(
        userId: String,
        mediaId: Int,
        episode: Int,
        timeAt: TimeAt,
    ): Flow<FirebaseResponse<Boolean>> = callbackFlow {
        val id = "${mediaId}_${episode}"
        db.collection(USER_FIREBASE)
            .document(userId)
            .collection(MY_WATCH_TIME)
            .document(id)
            .set(timeAt)
            .addOnSuccessListener {
                trySend(FirebaseResponse.Success(true))
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun getWatchTimeAt(
        userId: String,
        mediaId: Int,
        episode: Int
    ): Flow<FirebaseResponse<TimeAt>> = callbackFlow {
        val id = "${mediaId}_${episode}"
        db.collection(USER_FIREBASE)
            .document(userId)
            .collection(MY_WATCH_TIME)
            .document(id)
            .get()
            .addOnSuccessListener {
                if (it.exists() && it.data != null) {
                    val data = it.toObject<TimeAt>()
                    data?.let {
                        trySend(FirebaseResponse.Success(data))
                    }
                }
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }.flowOn(defaultDispatcher)

    override fun getMovie(mediaId: Int): Flow<FirebaseResponse<MovieFirebase>> = callbackFlow {
        db.collection(MOVIE_FIREBASE)
            .document(mediaId.toString())
            .get()
            .addOnSuccessListener {
                if (it.exists() && it.data != null) {
                    val data = it.toObject<MovieFirebase>()
                    data?.let {
                        trySend(FirebaseResponse.Success(data))
                    }
                }
            }
            .addOnFailureListener {
                trySend(FirebaseResponse.Error(it))
            }
        awaitClose {
            channel.close()
        }
    }
}