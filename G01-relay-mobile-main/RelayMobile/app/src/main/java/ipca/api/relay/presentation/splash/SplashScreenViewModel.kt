package ipca.api.relay.presentation.splash

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.domain.repository.AuthRepository
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.data.remote.api.AuthApi
import ipca.api.relay.data.remote.api.RetrofitInstance
import ipca.api.relay.data.repository.AuthException
import ipca.api.relay.data.repository.AuthRepositoryImpl
import ipca.api.relay.di.AppModule
import ipca.api.relay.presentation.recoverPassword.forgotPassword.ForgotPasswordViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SplashScreenViewModel(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _navigationEvent = Channel<SplashNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            resolve()
        }
    }

    private suspend fun resolve() {
        val destination = when {
            tokenManager.isAccessTokenValid() -> SplashNavigationEvent.NavigateToHome

            tokenManager.isRefreshTokenValid() -> {
                authRepository.refreshToken()
                    .fold(
                        onSuccess = { SplashNavigationEvent.NavigateToHome },
                        onFailure = { error ->
                            if (error is AuthException.NetworkError) SplashNavigationEvent.NavigateToHome
                            else SplashNavigationEvent.NavigateToLogin
                        }
                    )
            }

            else -> SplashNavigationEvent.NavigateToLogin
        }
        _navigationEvent.send(destination)
    }

    companion object {
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SplashScreenViewModel(
                    tokenManager = AppModule.getTokenManager(),
                    authRepository = AppModule.authRepository
                )
            }
        }
    }
}

sealed interface SplashNavigationEvent {
    data object NavigateToHome : SplashNavigationEvent
    data object NavigateToLogin : SplashNavigationEvent
}