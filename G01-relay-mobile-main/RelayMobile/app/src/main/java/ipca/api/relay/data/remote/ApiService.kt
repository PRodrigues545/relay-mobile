package ipca.api.relay.data.remote

import ipca.api.relay.data.model.MeetingPointRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("meeting-points")
    suspend fun createMeetingPoint(
        @Header("Authorization") token: String,
        @Body meetingPoint: MeetingPointRequest
    ): Response<Unit> // Response sem corpo, só sucesso/falha
}