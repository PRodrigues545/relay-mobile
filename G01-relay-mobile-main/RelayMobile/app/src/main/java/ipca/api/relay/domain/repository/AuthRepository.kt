package ipca.api.relay.domain.repository

import ipca.api.relay.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(username: String, email: String, password: String): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun refreshToken(): Result<Unit>

    suspend fun forgotPassword(email: String): Result<Unit>

    suspend fun verifyResetToken(token: String): Result<Unit>

    suspend fun resetPassword(token: String, newPassword: String): Result<Unit>
}