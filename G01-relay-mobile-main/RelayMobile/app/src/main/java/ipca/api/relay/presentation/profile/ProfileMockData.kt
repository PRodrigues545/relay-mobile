package ipca.api.relay.presentation.profile

// NOTE: the file name is historical. Mock data has been removed; this is now
// just the UI model definitions used across the profile screens. Renaming is
// safe but out of scope here — keep references stable.

data class ProfileUserUi(
    val id: Int = 0,
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val university: String = "",
    val sellerRating: Int = 0,
    val clientRating: Int = 0,
    val profilePicture: String? = null
) {
    /**
     * Public display rule: prefer the explicit `name` set by the user;
     * fall back to `username` when nothing is set.
     */
    val displayName: String
        get() = name.takeIf { it.isNotBlank() } ?: username
}

data class ReviewUi(
    val name: String,
    val rating: Int,
    val comment: String?
)

data class HistoryItemUi(
    val title: String,
    val description: String,
    val price: String,
    val person: String,
    val type: HistoryType
)

enum class HistoryType {
    PURCHASE,
    SALE,
    EXCHANGE
}

enum class ReviewTab {
    SELLER,
    CLIENT
}

/**
 * Empty starting point for ProfileViewModel.user. Replaces the previous
 * `mockUser` which seeded the state with placeholder values.
 */
val emptyProfileUser = ProfileUserUi()