package ipca.api.relay.domain.repository

import ipca.api.relay.domain.model.Notification

interface NotificationRepository {
    suspend fun getMyNotifications(): Result<List<Notification>>
    suspend fun markAsRead(id: Int): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
}