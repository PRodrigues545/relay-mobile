package ipca.api.relay.domain.model

data class RemoteCategory(
    val id: Int,
    val name: String,
    val description: String? = null
)