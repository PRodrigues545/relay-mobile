package ipca.api.relay.presentation.recoverPassword.validateToken

data class ValidateTokenUiState(
    val token: String = "",
    val isLoading : Boolean = false,
    val error: String? = null
)