package ipca.api.relay.presentation.product

data class ProductUiState(
    val imageUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val price: Float = 0f,
    val type: String = "SELL",
    val condition: String? = null,
    val borrowDuration: String? = null,
    val zones: String? = null,
    val sellerAvatarUrl: String? = null,
    val sellerName: String = "",
    val sellerUsername: String = "",
    val sellerReputation: Float = 0f,
    val resourceId: Int = 0,
    val sellerId: Int = 0,
    val isOwner: Boolean = false,
    /**
     * Becomes true after the first source of resource data has populated the
     * state — either the route-supplied ownerId, the local Room cache, or the
     * network. The bottom bar reads this to decide whether to paint at all,
     * so the user never sees the wrong call-to-action flash on screen before
     * the real one resolves.
     *
     * Stays true forever after the first resolution, even while a background
     * network refresh is running, because by then we already have enough
     * truth to render the right buttons.
     */
    val hasLoaded: Boolean = false,
    val showOfferDialog: Boolean = false,
    val offerPrice: String = "",
    val offerError: String? = null,
    val offerLoading: Boolean = false,
    val offerSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirm: Boolean = false,
    val deleteLoading: Boolean = false,
    val deleteSuccess: Boolean = false,
)