package ipca.api.relay.presentation.recoverPassword.resetPassword

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
import ipca.api.relay.presentation.auth.login.LoginViewModel
import ipca.api.relay.presentation.recoverPassword.forgotPassword.ForgotPasswordNavigationEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    val repository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(ResetPasswordUiState())
        private set

    private var resetToken = ""

    fun setToken(token: String){
        resetToken = token
    }

    fun onNewPasswordChange(newPassword: String){
        uiState = uiState.copy(newPassword = newPassword)
    }

    private val _navigationEvent = Channel<ResetPasswordNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onConfirmNewPassword(){
        val error = when {
            uiState.newPassword.isEmpty() -> "Password is required"
            uiState.newPassword.length < 6 -> "Password must be at least 6 characters long"
            else -> null
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.resetPassword(token = resetToken, newPassword = uiState.newPassword)
                .onSuccess {
                    // navigate away — use a Channel/event for this
                    _navigationEvent.send(ResetPasswordNavigationEvent.NavigateToLogin)
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
                ResetPasswordViewModel(
                    repository = AppModule.authRepository
                )
            }
        }
    }
}

sealed interface ResetPasswordNavigationEvent {
    data object NavigateToLogin: ResetPasswordNavigationEvent
}