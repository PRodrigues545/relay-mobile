package ipca.api.relay.domain.repository

import ipca.api.relay.di.AppModule

object TokenRepository {
    suspend fun sendTokenToBackend(token: String) {
        val tokenManager = AppModule.getTokenManager()
        val accessToken = tokenManager.getAccessToken() ?: return

        AppModule.userApi.updateDeviceToken(
            authorization = "Bearer $accessToken",
            body = DeviceTokenRequest(deviceToken = token)
        )
    }
}

data class DeviceTokenRequest(val deviceToken: String)