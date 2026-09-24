package ipca.api.relay

import android.app.Application
import ipca.api.relay.data.local.NotificationHelper
import ipca.api.relay.di.AppModule
import com.stripe.android.PaymentConfiguration

class RelayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51TaZDhDQmnfMWhQsKk5k6CALnLXZNrqFqWDoKGLysKvojrz4Xr1RDNZRnq9fsOSHBA34eUWqvUehzzvUeXSunoDK00n6QTcCk1" // tua chave pública
        )
        NotificationHelper.createChannel(this)
        AppModule.init(this, onSessionExpired = {
            AppModule.sessionExpiredEvent.trySend(Unit)
        })
        if (AppModule.getTokenManager().isAccessTokenValid()) {
            AppModule.connectSocket()
        }
    }
}