package com.catelt.mome.ui.auth.signup

import androidx.lifecycle.viewModelScope
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.repository.firebase.FirebaseRepositoryImpl
import com.catelt.mome.ui.auth.home.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    userManager: UserManager,
    googleSignInClient: GoogleSignInClient,
    private val firebaseRepository: FirebaseRepositoryImpl,
) : AuthViewModel(
    userManager,
    googleSignInClient,
    firebaseRepository,
) {

    fun signUp(email: String, password: String) {
        if (!validEmail(email) || !validPassword(password)) return

        viewModelScope.launch {
            firebaseRepository.createUserWithEmailAndPassword(email, password).collectLatest {
                it.handle(
                    success = { user ->
                        updateFirebaseAndUserManager(user)
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
}