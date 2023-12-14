package com.catelt.mome.ui.auth.home

import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.core.OnSignInStartedListener
import com.catelt.mome.data.model.account.User
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.repository.firebase.FirebaseRepositoryImpl
import com.catelt.mome.utils.USER_FIREBASE
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class AuthViewModel @Inject constructor(
    private val userManager: UserManager,
    private val googleSignInClient: GoogleSignInClient,
    private val firebaseRepository: FirebaseRepositoryImpl,
) : BaseViewModel() {
    val validEmailLiveData = MutableLiveData<String>()
    val validPasswordLiveData = MutableLiveData<String>()

    val isLogged = MutableStateFlow(userManager.isLogged())

    // GOOGLE
    fun signInWithGoogle(listener: OnSignInStartedListener) {
        listener.onSignInStarted(googleSignInClient)
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        signInWithCredential(credential)

    }

    private fun signInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            firebaseRepository.signInWithCredential(credential).collectLatest {
                it.handle(
                    success = { user ->
                        signUpOrSignIn(user)
                    },
                    error = { exception ->
                        checkFailDefault(exception.message.toString())
                    },
                    loading = {
                        isLoading.postValue(true)
                    }
                )
            }
        }
    }

    open fun checkFail(string: String) {
        with(string) {
            when {
                contains(KEY_NO_USER) -> toastMessage.postValue(WARNING_NO_USER)
                contains(KEY_INVALID_USER) -> toastMessage.postValue(WARNING_INVALID_USER)
                contains(KEY_ALREADY_EMAIL) -> toastMessage.postValue(WARNING_ALREADY_EMAIL)
                else -> {
                    toastMessage.postValue(SOMETHING_WRONG)
                }
            }
        }
    }

    private fun signUpOrSignIn(user: FirebaseUser) {
        viewModelScope.launch {
            firebaseRepository.checkExistedData(USER_FIREBASE, user.uid)
                .collectLatest { checkExist ->
                    checkExist.handle(
                        success = { isExisted ->
                            if (isExisted) {
                                getUserFirebase(user.uid)
                            } else {
                                updateFirebaseAndUserManager(user)
                            }
                        },
                        error = { exception ->
                            checkFailDefault(exception.message.toString())
                        }
                    )
                }
        }
    }

    protected fun updateFirebaseAndUserManager(user: FirebaseUser) {
        val account = User(
            user.uid,
            user.displayName.toString(),
            user.email.toString(),
            user.photoUrl.toString(),
        )

        viewModelScope.launch {
            firebaseRepository.setDataFirebase(USER_FIREBASE, user.uid, account).collectLatest {
                it.handle(
                    success = {
                        userManager.addAccount(account)
                        this.launch {
                            isLogged.emit(userManager.isLogged())
                        }
                    },
                    error = { exception ->
                        checkFailDefault(exception.message.toString())
                    }
                )
            }
        }
    }

    protected fun getUserFirebase(userId: String) {
        viewModelScope.launch {
            firebaseRepository.getDataFirebase(USER_FIREBASE, userId).collectLatest {
                it.handle(
                    success = { document ->
                        if (document.exists()) {
                            document.toObject<User>()?.let { account ->
                                userManager.addAccount(account)
                                viewModelScope.launch {
                                    isLogged.emit(userManager.isLogged())
                                }
                            }
                        }
                    },
                    error = { exception ->
                        checkFailDefault(exception.message.toString())
                    }
                )
            }
        }
    }

    protected fun checkFailDefault(string: String? = null) {
        val message = string ?: SOMETHING_WRONG
        checkFail(message)
        isLoading.postValue(false)
    }

    fun validEmail(emailText: String): Boolean {
        return if (emailText.isBlank()) {
            validEmailLiveData.postValue("Mustn't empty")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            validEmailLiveData.postValue("Invalid Email Address")
            false
        } else {
            validEmailLiveData.postValue("")
            true
        }
    }

    fun validPassword(passwordText: String): Boolean {
        return if (passwordText.length < 8) {
            validPasswordLiveData.postValue("Minimum 8 Character Password")
            false
        } else if (!passwordText.matches(".*[A-Z].*".toRegex())) {
            validPasswordLiveData.postValue("Must Contain 1 Upper-case Character")
            false
        } else if (!passwordText.matches(".*[a-z].*".toRegex())) {
            validPasswordLiveData.postValue("Must Contain 1 Lower-case Character")
            false
        } else {
            validPasswordLiveData.postValue("")
            true
        }
    }

    protected fun validPasswordLogin(passwordText: String): Boolean {
        return if (passwordText.isEmpty()) {
            validPasswordLiveData.postValue("Mustn't empty")
            false
        } else {
            validPasswordLiveData.postValue("")
            true
        }
    }

    companion object {
        const val KEY_NO_USER = "user may have been deleted"
        const val WARNING_NO_USER = "Email does not exist"
        const val KEY_INVALID_USER = "password is invalid"
        const val WARNING_INVALID_USER = "Wrong password"
        const val KEY_BLOCK = "due to many failed login attempts"
        const val WARNING_BLOCK = "The device was blocked login about"
        const val KEY_ALREADY_EMAIL = "The email address is already"
        const val WARNING_ALREADY_EMAIL = "The email address is already"
        const val SOMETHING_WRONG = "Something wrong"

        const val EMAIL = "email"
        const val PUBLIC_PROFILE = "public_profile"
        const val USER_FRIEND = "user_friends"
    }
}