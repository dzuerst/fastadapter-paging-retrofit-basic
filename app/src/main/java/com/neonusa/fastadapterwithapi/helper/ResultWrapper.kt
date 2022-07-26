package com.neonusa.fastadapterwithapi.helper

sealed class ResultWrapper {

    object Loading : ResultWrapper()

    data class  Error(val errorMessage: String) : ResultWrapper()

    data class Success<T>(val data: T) : ResultWrapper()
}