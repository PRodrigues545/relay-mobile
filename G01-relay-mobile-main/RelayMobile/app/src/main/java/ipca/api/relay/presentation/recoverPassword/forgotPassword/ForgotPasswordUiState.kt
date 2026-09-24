package ipca.api.relay.presentation.recoverPassword.forgotPassword

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading : Boolean = false,
    val error: String? = null
)