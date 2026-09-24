package ipca.api.relay.domain.model

data class Resource(
    val id: Int,
    val title: String,
    val description: String = "",
    val price: Double?,
    val category: String,
    val categoryId: Int = 0,
    val imageUrl: String?,
    val isAvailable: Boolean,
    val type: String = "SELL",         // SELL, BORROW, BOTH
    val condition: String? = null,      // NEW, LIKE_NEW, GOOD, FAIR, POOR
    val borrowDuration: String? = null,
    val zones: String? = null,
    val ownerId: Int = 0,
    val ownerName: String = "",
    val ownerUsername: String = "",
    val ownerReputation: Int = 75,
    val ownerPicture: String? = null
) {
    val ownerDisplayName: String
        get() = ownerName.takeIf { it.isNotBlank() } ?: ownerUsername
}