package ipca.api.relay.data.local

import android.content.Context

/**
 * Cached user credentials, written after a successful login/register so the
 * profile screen can render immediately without waiting on /users/me. The
 * canonical source is always the backend — these defaults intentionally fall
 * back to empty strings (no demo / placeholder identity).
 */
class ProfileLocalStore(context: Context) {

    private val prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE)

    fun saveUser(name: String, email: String, username: String) {
        prefs.edit()
            .putString("name", name)
            .putString("email", email)
            .putString("username", username)
            .apply()
    }

    fun getName(): String = prefs.getString("name", "").orEmpty()
    fun getEmail(): String = prefs.getString("email", "").orEmpty()
    fun getUsername(): String = prefs.getString("username", "").orEmpty()

    fun updateUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
