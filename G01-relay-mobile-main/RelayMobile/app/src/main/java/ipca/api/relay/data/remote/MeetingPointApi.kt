package ipca.api.relay.data.remote

import android.content.Context
import ipca.api.relay.data.local.TokenManager
import ipca.api.relay.presentation.meetingpoint.MeetingPointUi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// API interface
interface MeetingPointService {
    @POST("/meeting-points")
    fun saveMeetingPoint(
        @Header("Authorization") token: String, // "Bearer <token>"
        @Body point: MeetingPointUi
    ): Call<Map<String, Any>>
}

// Retrofit client
private val retrofit = Retrofit.Builder()
    .baseUrl("http://10.0.2.2:3003/") // ⚠️ Emulador Android usa 10.0.2.2
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val service = retrofit.create(MeetingPointService::class.java)

// Função para salvar o ponto usando TokenManager
fun saveMeetingPoint(context: Context, point: MeetingPointUi) {
    val tokenManager = TokenManager(context)
    val token = tokenManager.getAccessToken()

    if (token == null) {
        println("❌ Access token not found. Login required.")
        return
    }

    service.saveMeetingPoint("Bearer $token", point).enqueue(object : Callback<Map<String, Any>> {
        override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
            if (response.isSuccessful) {
                println("✅ Meeting point saved: ${response.body()}")
            } else {
                println("❌ Failed to save point: ${response.code()} - ${response.message()}")
            }
        }

        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
            println("❌ Network error: ${t.message}")
        }
    })
}