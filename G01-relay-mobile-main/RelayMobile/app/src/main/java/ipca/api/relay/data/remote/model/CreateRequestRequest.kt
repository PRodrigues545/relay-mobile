package ipca.api.relay.data.remote.model

data class CreateRequestRequest(
    val title: String,
    val description: String,
    val category_id: Int = 1 // Garante que o nome é igual ao que o Node espera
)