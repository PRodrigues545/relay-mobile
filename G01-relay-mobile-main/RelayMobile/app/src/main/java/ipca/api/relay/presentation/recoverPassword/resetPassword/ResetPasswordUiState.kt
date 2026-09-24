package ipca.api.relay.presentation.recoverPassword.resetPassword

data class ResetPasswordUiState(
    val newPassword: String = "",
    val isLoading : Boolean = false,
    val error: String? = null
)