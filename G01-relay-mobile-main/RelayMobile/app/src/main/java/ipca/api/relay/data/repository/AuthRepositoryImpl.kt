package ipca.api.relay.data.repository

import android.util.Log
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.data.remote.api.AuthApi
import ipca.api.relay.data.remote.api.RetrofitInstance
import ipca.api.relay.data.remote.model.ForgotPasswordRequest
import ipca.api.relay.data.remote.model.LoginRequest
import ipca.api.relay.data.remote.model.RefreshRequest
import ipca.api.relay.data.remote.model.RegisterRequest
import ipca.api.relay.data.remote.model.ResetPasswordRequest
import ipca.api.relay.data.remote.model.VerifyResetTokenRequest
import ipca.api.relay.domain.model.User
import ipca.api.relay.domain.repository.AuthRepository
import retrofit2.HttpException
import java.io.IOException

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
    //private val userDao: UserDao
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            // Login doesn't return user info — fetch it or build minimally
            Result.success(User(id = "", name = "", email = email))
        } catch (e: HttpException) {
            Result.failure(when (e.code()) {
                401 -> AuthException.InvalidCredentials
                else -> AuthException.ServerError(e.code())
            })
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Unexpected error: ${e::class.simpleName}: ${e.message}")
            Result.failure(e)
        }
    }
    override suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            authApi.register(RegisterRequest(username, email, password))
            login(email, password).also { result ->
                if (result.isFailure)
                    Log.w("AuthRepositoryImpl", "Register succeeded but auto-login failed")
            }
        } catch (e: HttpException) {
            Result.failure(when (e.code()) {
                409 -> AuthException.UserAlreadyExists
                else -> AuthException.ServerError(e.code())
            })
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }
    override suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken != null) {
                authApi.logout(refreshToken) // no need to inspect the response
            }
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: HttpException) {
            // Still clear tokens locally even if server call fails
            tokenManager.clearTokens()
            Result.success(Unit)
        } catch (e: IOException) {
            tokenManager.clearTokens()
            Result.success(Unit)
        }
    }
    override suspend fun refreshToken(): Result<Unit> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(IllegalStateException("No refresh token"))
        return try {
            val response = authApi.refresh(RefreshRequest(refreshToken))
            tokenManager.saveTokens(response.accessToken, response.refreshToken)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            authApi.forgotPassword(ForgotPasswordRequest(email))
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun verifyResetToken(token: String): Result<Unit> {
        return try {
            authApi.verifyResetToken(VerifyResetTokenRequest(token))
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }

    override suspend fun resetPassword(token: String, newPassword: String): Result<Unit> {
        return try {
            authApi.resetPassword(ResetPasswordRequest(token, newPassword))
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(AuthException.ServerError(e.code()))
        } catch (e: IOException) {
            Result.failure(AuthException.NetworkError(e))
        }
    }
}

// Sealed class for typed auth errors
sealed class AuthException : Exception() {
    data object UserAlreadyExists : AuthException()
    data object InvalidCredentials : AuthException()
    data class ServerError(val code: Int) : AuthException()
    data class NetworkError(override val cause: Throwable) : AuthException()
}