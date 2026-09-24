package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.ForgotPasswordRequest
import ipca.api.relay.data.remote.model.ForgotPasswordResponse
import ipca.api.relay.data.remote.model.LoginRequest
import ipca.api.relay.data.remote.model.LoginResponse
import ipca.api.relay.data.remote.model.RefreshRequest
import ipca.api.relay.data.remote.model.RefreshResponse
import ipca.api.relay.data.remote.model.RegisterRequest
import ipca.api.relay.data.remote.model.RegisterResponse
import ipca.api.relay.data.remote.model.ResetPasswordRequest
import ipca.api.relay.data.remote.model.ResetPasswordResponse
import ipca.api.relay.data.remote.model.VerifyResetTokenRequest
import ipca.api.relay.data.remote.model.VerifyResetTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest) : LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest) : RegisterResponse

    @POST("auth/refreshToken")
    suspend fun refresh(@Body request: RefreshRequest) : RefreshResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest) : ForgotPasswordResponse

    @POST("auth/verify-reset-token")
    suspend fun verifyResetToken(@Body request: VerifyResetTokenRequest) : VerifyResetTokenResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest) : ResetPasswordResponse

    @DELETE("auth/logout")
    suspend fun logout(
        @Header("x-refresh-token") refreshToken: String
    ) : Response<Unit>
}