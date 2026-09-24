package ipca.api.relay.data.local

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import android.util.Log

class TokenManager(private val context: Context) {

    private val prefs by lazy {
        createEncryptedPrefsSafely()
    }

    private fun createEncryptedPrefsSafely() =
        try {
            EncryptedSharedPreferences.create(
                context,
                "auth",
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.deleteSharedPreferences("auth")

            EncryptedSharedPreferences.create(
                context,
                "auth",
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

    fun saveTokens(accessToken: String, refreshToken: String) {


        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return try {
            prefs.getString("access_token", null)
        } catch (e: Exception) {
            clearTokens()
            null
        }
    }

    fun getRefreshToken(): String? {
        return try {
            prefs.getString("refresh_token", null)
        } catch (e: Exception) {
            clearTokens()
            null
        }
    }

    fun clearTokens() {
        try {
            prefs.edit()
                .remove("access_token")
                .remove("refresh_token")
                .apply()
        } catch (e: Exception) {
            context.deleteSharedPreferences("auth")
        }
    }

    fun isAccessTokenValid(): Boolean {
        val accessToken = getAccessToken() ?: return false
        return !isTokenExpired(accessToken)
    }

    fun isRefreshTokenValid(): Boolean {
        val refreshToken = getRefreshToken() ?: return false
        return !isTokenExpired(refreshToken)
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
            val exp = JSONObject(decoded).getLong("exp")
            System.currentTimeMillis() / 1000 >= exp - 30
        } catch (e: Exception) {
            true
        }
    }
}