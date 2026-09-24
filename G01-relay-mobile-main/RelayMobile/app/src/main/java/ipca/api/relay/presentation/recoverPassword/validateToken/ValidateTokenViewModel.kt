package ipca.api.relay.presentation.recoverPassword.validateToken

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.data.remote.api.AuthApi
import ipca.api.relay.data.remote.api.RetrofitInstance
import ipca.api.relay.data.repository.AuthException
import ipca.api.relay.data.repository.AuthRepositoryImpl
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ValidateTokenViewModel(
    val repository: AuthRepository
) : ViewModel() {

    var uiState by mutableStateOf(ValidateTokenUiState())
        private set

    private val _navigationEvent = Channel<ValidateTokenNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onTokenChange(token: String) {
        uiState = uiState.copy(token = token)
    }

    fun onVerifyToken(){
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.verifyResetToken(token = uiState.token.trim())
                .onSuccess {
                    _navigationEvent.send(
                        ValidateTokenNavigationEvent.NavigateToResetPassword(uiState.token.trim())
                    )
                }
                .onFailure { error ->
                    val message = when (error) {
                        is AuthException.NetworkError -> "No internet connection"
                        else -> "Invalid or expired token"
                    }
                    uiState = uiState.copy(error = message)
                }
            uiState = uiState.copy(isLoading = false)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ValidateTokenViewModel(
                    repository = AppModule.authRepository
                )
            }
        }
    }
}

sealed interface ValidateTokenNavigationEvent {
    data class NavigateToResetPassword(val token: String) : ValidateTokenNavigationEvent
}