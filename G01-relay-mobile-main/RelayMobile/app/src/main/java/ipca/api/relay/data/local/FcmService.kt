package ipca.api.relay.data.local

import ipca.api.relay.data.local.NotificationHelper
import ipca.api.relay.domain.repository.TokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {

    // Called when a new token is generated (first launch or token refresh)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TokenRepository.sendTokenToBackend(token)
            } catch (e: Exception) {
                // Token will be sent on next login if this fails
            }
        }
    }

    // Called when a notification arrives while app is in foreground
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            NotificationHelper.show(applicationContext, it.title ?: "", it.body ?: "")
        }
    }
}