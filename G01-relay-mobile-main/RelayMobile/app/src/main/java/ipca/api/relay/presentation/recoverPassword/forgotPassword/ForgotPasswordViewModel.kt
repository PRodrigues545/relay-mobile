package ipca.api.relay.presentation.recoverPassword.forgotPassword

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
import ipca.api.relay.presentation.auth.login.LoginNavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    val repository: AuthRepository
): ViewModel(){
    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    private val _navigationEvent = Channel<ForgotPasswordNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onEmailChange(email: String){
        uiState = uiState.copy(email = email)
    }

    fun onRecoverPassword(){
        val error = when {
            uiState.email.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches() -> "Invalid email"
            else -> null
        }

        if (error != null) {
            uiState = uiState.copy(error = error)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.forgotPassword(email = uiState.email.trim())
                .onSuccess {
                    // navigate away — use a Channel/event for this
                    _navigationEvent.send(ForgotPasswordNavigationEvent.NavigateToRecoverPassword)
                }
                .onFailure { error ->
                    val message = when (error) {
                        is AuthException.InvalidCredentials -> "Invalid email or password"
                        is AuthException.NetworkError -> "No internet connection"
                        else -> "Something went wrong"
                    }
                    uiState = uiState.copy(error = message)
                }
            uiState = uiState.copy(isLoading = false)
        }
        return
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ForgotPasswordViewModel(
                    repository = AppModule.authRepository
                )
            }
        }
    }
}

sealed interface ForgotPasswordNavigationEvent {
    data object NavigateToRecoverPassword : ForgotPasswordNavigationEvent
}