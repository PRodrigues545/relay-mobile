package ipca.api.relay.domain.model

data class AddressPrediction(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullText: String
)