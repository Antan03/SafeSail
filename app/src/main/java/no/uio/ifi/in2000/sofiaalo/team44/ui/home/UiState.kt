package no.uio.ifi.in2000.sofiaalo.team44.ui.home

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val throwable: Throwable? = null) : UiState<Nothing>()
}
