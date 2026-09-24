package ipca.api.relay.data.remote.api

import ipca.api.relay.data.remote.dto.meetingpoint.MeetingPointDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MeetingPointApi {

    @POST("operation/meeting-points")
    suspend fun saveMeetingPoint(
        @Body dto: MeetingPointDto
    ): Response<Unit>
}