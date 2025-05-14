package com.boa.test.city.seeker.domain.model

/**
 * A sealed class representing the different states of a UI element or data source.
 *
 * This class is designed to hold data, error messages, and loading status to provide a clear
 * and consistent way to manage UI states.
 *
 * @param T The type of data held by this UI state.
 * @property data The data associated with the current state (nullable).
 * @property message An optional message associated with the state, often used for error messages.
 * @property isLoading A boolean indicating whether the UI is currently in a loading state.
 */
sealed class UiStateModel<T>(
    val data: T? = null,
    val message: String = "",
    val isLoading: Boolean = false
) {
    /**
     * Represents a successful state with data.
     *
     * @param T The type of data held by this state.
     * @property data The data associated with the successful state.
     */
    class Success<T>(data: T) : UiStateModel<T>(data)

    /**
     * Represents a loading state in the UI.
     *
     * @param T The type of data that is being loaded (though the data is not available in this state).
     * @property isLoading Indicates whether the UI is currently in a loading state.
     */
    class Loading<T>(isLoading: Boolean) : UiStateModel<T>(isLoading = isLoading)

    /**
     * Represents an error state in the UI.
     *
     * @param T the type of data associated with the UI state (though not available in this state).
     * @property message the error message describing the issue.
     */
    class Error<T>(message: String) : UiStateModel<T>(message = message)
}
