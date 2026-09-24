package ipca.api.relay.data.remote.dto.meetingpoint

data class MeetingPointDto(
    val requestId: Int,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double
)