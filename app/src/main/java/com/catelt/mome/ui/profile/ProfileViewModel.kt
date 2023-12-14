package com.catelt.mome.ui.profile

import androidx.lifecycle.viewModelScope
import com.catelt.mome.core.BaseViewModel
import com.catelt.mome.data.model.account.UserManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userManager: UserManager,
    private val googleSignInClient: GoogleSignInClient,
    private val firebaseAuth: FirebaseAuth
) : BaseViewModel() {
    val isLogged = MutableStateFlow(userManager.isLogged())

    fun getName(): String {
        return userManager.getName()
    }

    fun logOut() {
        firebaseAuth.signOut()
        userManager.logOut()
        //Google SignOut
        googleSignInClient.signOut()
        viewModelScope.launch {
            isLogged.emit(userManager.isLogged())
        }
    }
}