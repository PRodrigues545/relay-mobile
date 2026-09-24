package ipca.api.relay.data.repository

import ipca.api.relay.data.remote.api.NotificationsApi
import ipca.api.relay.domain.model.Notification
import ipca.api.relay.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val api: NotificationsApi
) : NotificationRepository{
    override suspend fun getMyNotifications(): Result<List<Notification>> {
        return try {
            Result.success(api.getMyNotifications().map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(id: Int): Result<Unit> {
        return try {
            api.markAsRead(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            api.markAllAsRead()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}