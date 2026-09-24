package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.model.NotificationDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationsApi {
    @GET("notifications/")
    suspend fun getMyNotifications(): List<NotificationDto>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): Response<Unit>

    @PATCH("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>
}