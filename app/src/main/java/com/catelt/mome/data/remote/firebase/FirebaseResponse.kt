package com.catelt.mome.data.remote.firebase

/**
 * A generic class that holds a value or error.
 * @param <T>
 */
sealed class FirebaseResponse<out R> {

    data class Success<out T>(val data: T) : FirebaseResponse<T>()
    data class Error(val exception: Exception) : FirebaseResponse<Nothing>()
    object Loading : FirebaseResponse<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Loading -> "Loading"
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }

    fun handle(
        success: (R) -> Unit,
        error: (Exception) -> Unit = {},
        loading: () -> Unit = {},
    ){
        when(this){
            is Success<*> -> {
                success(data as R)
            }
            is Error -> {
                error(exception)
            }
            is Loading -> {
                loading()
            }
        }
    }

}

/**
 * `true` if [ResultCallBack] is of type [Success] & holds non-null [Success.data].
 */
val FirebaseResponse<*>.succeeded
    get() = this is FirebaseResponse.Success && data != null