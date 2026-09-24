package ipca.api.relay.data.remote.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val id: Int,
    val email: String,
    val message: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class VerifyResetTokenRequest(
    val token: String
)

data class VerifyResetTokenResponse(
    val message: String
)

data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val message: String
)