package com.catelt.mome.ui.auth.signin

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.catelt.mome.data.model.account.UserManager
import com.catelt.mome.data.repository.firebase.FirebaseRepositoryImpl
import com.catelt.mome.ui.auth.home.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    userManager: UserManager,
    googleSignInClient: GoogleSignInClient,
    private val firebaseRepository: FirebaseRepositoryImpl,
) : AuthViewModel(
    userManager,
    googleSignInClient,
    firebaseRepository,
) {
    private var remain = 3
    private val tokenDevice = MutableLiveData("")
    val isBlock = MutableLiveData(false)

    init {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            tokenDevice.postValue(it)
            getTimeBlock(it)
        }
    }

    private fun setTimeBlock() {
        if (!tokenDevice.value.isNullOrBlank()) {
            viewModelScope.launch {
                firebaseRepository.setDataFirebase(
                    BLOCK_DEVICE,
                    tokenDevice.value ?: "",
                    hashMapOf(TIME to (Date().time + TIME_BLOCK))
                ).collectLatest {
                    it.handle(
                        success = {},
                        error = { exception ->
                            checkFailDefault(exception.message.toString())
                        }
                    )
                }
            }
        }
    }

    private fun deleteTimeBlock() {
        if (!tokenDevice.value.isNullOrBlank()) {
            viewModelScope.launch {
                firebaseRepository.deleteDataFirebase(BLOCK_DEVICE, tokenDevice.value ?: "")
                    .collectLatest {
                        it.handle(
                            success = {},
                            error = { exception ->
                                checkFailDefault(exception.message.toString())
                            }
                        )
                    }
            }
        }
    }

    private fun getTimeBlock(token: String) {
        viewModelScope.launch {
            firebaseRepository.getDataFirebase(BLOCK_DEVICE, token).collectLatest {
                it.handle(
                    success = { document ->
                        if (document.exists() && document.data != null) {
                            val time = document.get(TIME) as Long - Date().time
                            if (time < 0) {
                                resetBlock()
                            } else {
                                checkFailDefault("$WARNING_BLOCK ${time % 60000 / 1000} second")
                                blockLogin(time)
                            }
                        } else {
                            resetBlock()
                        }
                    },
                    error = { exception ->
                        checkFailDefault(exception.message.toString())
                    }
                )
            }
        }
    }

    private fun blockLogin(time: Long = TIME_BLOCK.toLong()) {
        object : CountDownTimer(time, COUNT_DOWN.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                isBlock.postValue(true)
            }

            override fun onFinish() {
                resetBlock()
            }
        }.start()
    }

    fun resetBlock() {
        isBlock.postValue(false)
        remain = 3
        deleteTimeBlock()
    }


    fun signIn(email: String, password: String) {
        if (!validEmail(email) || !validPasswordLogin(password)) {
            return
        }
        viewModelScope.launch {
            firebaseRepository.signInWithEmailAndPassword(email, password).collectLatest {
                it.handle(
                    success = { user ->
                        getUserFirebase(user.uid)
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

    override fun checkFail(string: String) {
        remain -= 1
        if (remain == 0) {
            setTimeBlock()
            blockLogin()
        }
        if (string.contains(KEY_BLOCK)) {
            setTimeBlock()
            blockLogin()
            checkFailDefault("$WARNING_BLOCK ${TIME_BLOCK / 1000} second")
        } else {
            super.checkFail(string)
        }
    }

    fun forgotPassword(emailText: String) {
        if (!validEmail(emailText)) {
            return
        }
        if (emailText.isBlank()) {
            checkFailDefault("Please enter email")
        } else {
            viewModelScope.launch {
                firebaseRepository.sendPasswordResetEmail(emailText).collectLatest {
                    it.handle(
                        success = {
                            checkFailDefault("Email sent.")
                        },
                        error = {
                            checkFailDefault("Email invalid")
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val TIME_BLOCK = 30000
        const val COUNT_DOWN = 1000
        const val TIME = "time"
        const val BLOCK_DEVICE = "block"
    }
}