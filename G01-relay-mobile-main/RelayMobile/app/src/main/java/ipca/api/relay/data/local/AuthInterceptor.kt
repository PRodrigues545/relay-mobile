package ipca.api.relay.data.local

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val onSessionExpired: () -> Unit
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getAccessToken()

        val request = chain.request().newBuilder()
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        val response = chain.proceed(request)

        if(response.code == 401){
            tokenManager.clearTokens()
            onSessionExpired()
        }

        return response
    }
}