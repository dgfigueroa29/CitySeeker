package com.boa.test.city.seeker.domain.model

sealed class UiStateModel<T>(
    val data: T? = null,
    val message: String = "",
    val isLoading: Boolean = false
) {
    class Success<T>(data: T) : UiStateModel<T>(data)
    class Loading<T>(isLoading: Boolean) : UiStateModel<T>(isLoading = isLoading)
    class Error<T>(message: String) : UiStateModel<T>(message = message)
}
