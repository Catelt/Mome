package com.catelt.mome.core

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catelt.mome.data.remote.api.ApiResponse
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    val isLoading = MutableLiveData(false)
    val toastMessage = MutableLiveData("")

    private val _error: MutableSharedFlow<String?> = MutableSharedFlow(replay = 0)
    val error: StateFlow<String?> = _error.asSharedFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(10), null
    )

    protected fun <T> onError(response: ApiResponse.Exception<T>) {
        FirebaseCrashlytics.getInstance().recordException(response.exception)

        viewModelScope.launch {
            _error.emit(response.exception.localizedMessage)
        }
    }

    protected fun <T> onFailure(response: ApiResponse.Failure<T>) {
        viewModelScope.launch {
            _error.emit(response.apiError.statusMessage)
        }
    }
}