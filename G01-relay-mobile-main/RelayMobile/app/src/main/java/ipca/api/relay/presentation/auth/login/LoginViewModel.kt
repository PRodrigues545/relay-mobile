package ipca.api.relay.presentation.auth.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import ipca.api.relay.data.repository.AuthException
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.AuthRepository
import ipca.api.relay.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: AuthRepository
): ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _navigationEvent = Channel<LoginNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

//    private val _events = Channel<UiEvent>()
//    val events = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onPasswordVisibilityToggle() {
        uiState = uiState.copy(showPassword = !uiState.showPassword)
    }

    fun onLoginClick(){
        val emailError = when {
            uiState.email.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches() -> "Invalid email"
            else -> null
        }

        val passwordError = when {
            uiState.password.isEmpty() -> "Password is required"
            //uiState.password.length < 8 -> "Password must be at least 8 characters"
            else -> null
        }

        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, emailError = null, passwordError = null)
            repository.login(uiState.email.trim(), uiState.password)
                .onSuccess {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        viewModelScope.launch(Dispatchers.IO) {
                            TokenRepository.sendTokenToBackend(token)
                        }
                    }
                    AppModule.connectSocket()  // add this
                    // navigate away — use a Channel/event for this
                    _navigationEvent.send(LoginNavigationEvent.NavigateToHome)
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

    fun onForgotPasswordClick(){
        viewModelScope.launch {
            _navigationEvent.send(LoginNavigationEvent.NavigateToForgotPassword)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                LoginViewModel(
                    repository = AppModule.authRepository
                )
            }
        }
    }
}

sealed interface LoginNavigationEvent {
    data object NavigateToHome : LoginNavigationEvent
    data object NavigateToForgotPassword : LoginNavigationEvent
}