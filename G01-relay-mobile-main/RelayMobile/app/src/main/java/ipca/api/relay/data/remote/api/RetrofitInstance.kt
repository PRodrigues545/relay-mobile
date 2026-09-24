package ipca.api.relay.data.remote.api

import ipca.api.relay.BuildConfig
import ipca.api.relay.data.local.AuthInterceptor
import ipca.api.relay.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // BODY-level logging dumps full request/response payloads — including the
    // Authorization header — into logcat. That's fine for local development,
    // but it must never run on a release build, where an attacker with logcat
    // access (e.g. a connected device, an exfiltration sink) could read tokens.
    // Gated on BuildConfig.DEBUG so release strips it to NONE.
    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
            // Belt-and-braces: even on debug, never log the bearer token.
            redactHeader("Authorization")
        }
    }

    fun buildClient(tokenManager: TokenManager, onSessionExpired: () -> Unit): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager, onSessionExpired))
            .addInterceptor { chain ->
                // Required so the ngrok dev tunnel doesn't gate requests behind
                // its HTML interstitial. Harmless on prod (header is ignored).
                val request = chain.request().newBuilder()
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            // 10s connect is reasonable. read/write are bumped: 10s for read
            // was too aggressive for image responses on flaky mobile networks
            // (Coil retries paper over this in the UI, but other API responses
            // would just fail). 30s read / 60s write matches the call timeout.
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun buildRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://eva-insensitive-mutually.ngrok-free.dev/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}