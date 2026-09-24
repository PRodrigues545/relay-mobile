package ipca.api.relay.data.model

data class MeetingPointRequest(
    val requestId: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)