package ipca.api.relay.navigation

import kotlinx.serialization.Serializable

// --- GRAFOS (Root e Tabs) ---
@Serializable object AuthGraph
@Serializable object MainGraph
@Serializable object HomeGraph
@Serializable object RequestsGraph
@Serializable object CreateGraph
@Serializable object ProfileGraph
@Serializable object ProductGraph
@Serializable object MessagesGraph
// SearchGraph removed: the entire search feature (graph, screen, viewmodel,
// ui state) was commented out and unreachable. See deletion list in the
// optimization notes. If/when search is reinstated, restore both the
// SearchGraph object here and the screen/graph/vm files.

// --- ECRÃS ---
@Serializable object Splash
@Serializable object Login
@Serializable object Register
@Serializable object ForgotPassword
@Serializable object ValidateToken
@Serializable data class ResetPassword(val token: String)

@Serializable object Home
// `Search` route also removed — was the start destination of the dead SearchGraph.
@Serializable object CreateRequest
@Serializable object CreateResource

@Serializable data class EditResource(val id: Int)

// --- Purchase / Borrow flows ---
@Serializable data class Purchase(val resourceId: Int)
@Serializable data class Borrow(val resourceId: Int)
@Serializable object PaymentMethodPicker

@Serializable object Profile
// ownerId is optional so call sites that already know it (Home, Profile listings,
// search results) can pass it through and ProductScreen renders the correct
// bottom bar — Owner vs Sell/Borrow — on the first frame instead of after the
// network round-trip. Deep-link entry points (notifications, share intents)
// can omit it and the screen falls back to its cache/network resolution path.
@Serializable data class Product(val id: Int, val ownerId: Int? = null)

// Other user's public profile. Used from the seller block on ProductScreen.
@Serializable data class UserProfile(val id: Int)

@Serializable data class UserReviews(val userId: Int)

@Serializable object Conversations
// otherUserDisplayName is optional with a null default so any existing
// constructor call sites that don't pass it still compile. MessagesScreen
// prefers it over otherUserUsername in its top bar.
@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val otherUserAvatar: String? = null,
    val otherUserUsername: String? = null,
    val otherUserDisplayName: String? = null
)

// Ecrãs de Perfil
@Serializable object EditProfile
@Serializable object Settings
@Serializable object History
@Serializable object SellerReviews
@Serializable object ClientReviews
@Serializable object ChangeUsername
@Serializable data class TransactionDetail(val id: Int, val type: String)

// --- Reviews ---
@Serializable object PendingReviews
@Serializable data class WriteReview(
    val transactionId: Int,
    val transactionType: String,
    val reviewedName: String,
    val reviewedPicture: String? = null,
)

// Meeting point navigation routes

@Serializable
data class MeetingPointSelection(val productId: Int)

@Serializable
data class MeetingPointConfirmation(
    val id: Int,
    val pointName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val selectedTime: String
)

@Serializable
data class MeetingPointDetails(
    val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable object Notifications