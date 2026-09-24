package ipca.api.relay.presentation.product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.Resource
import ipca.api.relay.domain.repository.MessagesRepository
import ipca.api.relay.domain.repository.ResourceRepository
import ipca.api.relay.presentation.messages.messages.decodeUserId
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ResourceRepository,
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProductUiState())
        private set

    private val _navigationEvent = Channel<ProductNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val currentUserId: Int =
        AppModule.getTokenManager().getAccessToken()
            ?.let { decodeUserId(it) } ?: -1

    /**
     * Loads a resource into [uiState] with up to three sources, in order of
     * how fast they can give us *some* truth:
     *
     *   1. [ownerIdHint] — the caller (typically the navigation route) tells
     *      us upfront who owns the resource. This is enough to render the
     *      correct bottom bar (Owner vs Buy/Borrow) on the very first frame,
     *      before any I/O. ~0ms.
     *
     *   2. The Room cache — if the user has seen this resource before in
     *      this install, the full state is available locally. Typical cost
     *      5–15ms, well inside one frame.
     *
     *   3. The network — fresh data from the server. Whatever it returns
     *      becomes the source of truth and replaces the previous snapshot.
     *
     * All three converge on the same UI state. The user only ever sees the
     * "loading" feel if NONE of the first two has data — which in practice
     * is just the deep-link-from-cold-start case.
     *
     * [hasLoaded] flips to true as soon as ANY of the three has populated
     * the state, and never flips back. ProductScreen's bottom bar reads this
     * flag and renders a neutral placeholder until it's true, so the wrong
     * call-to-action can never flash on screen.
     */
    fun loadResource(id: Int, ownerIdHint: Int? = null) {
        // Step 1: instant ownership hint. We don't have the rest of the
        // resource yet, but we know which bottom bar to render.
        if (ownerIdHint != null && !uiState.hasLoaded) {
            uiState = uiState.copy(
                resourceId = id,
                sellerId = ownerIdHint,
                isOwner = ownerIdHint == currentUserId,
                hasLoaded = true
            )
        }

        viewModelScope.launch {
            // Step 2: cache. If the route gave us a hint, the bottom bar is
            // already correct — the cache fills in the title, price, image,
            // etc. If there was no hint, the cache also resolves ownership.
            val cached = repository.getCachedResource(id)
            if (cached != null) {
                applyResource(cached)
            }

            // Step 3: network refresh. Always runs (stale-while-revalidate).
            // If the cache miss happened above, this is what finally
            // populates the screen — and it's also the only path that flips
            // hasLoaded when neither hint nor cache had anything to say.
            val fresh = runCatching { repository.getResource(id) }.getOrNull()
            if (fresh != null) {
                applyResource(fresh)
            } else if (!uiState.hasLoaded) {
                // No hint, no cache, network failed — show the screen anyway
                // with whatever defaults we have so the user isn't stuck on
                // a blank frame. The error field stays null because there's
                // no actionable retry yet.
                uiState = uiState.copy(hasLoaded = true)
            }
        }
    }

    private fun applyResource(resource: Resource) {
        uiState = uiState.copy(
            resourceId = resource.id,
            sellerId = resource.ownerId,
            isOwner = resource.ownerId == currentUserId,
            title = resource.title,
            description = resource.description,
            price = resource.price?.toFloat() ?: 0f,
            type = resource.type,
            condition = resource.condition,
            borrowDuration = resource.borrowDuration,
            zones = resource.zones,
            imageUrl = resource.imageUrl,
            sellerName = resource.ownerDisplayName,
            sellerUsername = resource.ownerUsername,
            sellerReputation = resource.ownerReputation / 20.0f,
            sellerAvatarUrl = resource.ownerPicture,
            hasLoaded = true
        )
    }

    // ── Delete ──────────────────────────────────────────────
    fun onRequestDelete() {
        uiState = uiState.copy(showDeleteConfirm = true)
    }

    fun onDismissDelete() {
        uiState = uiState.copy(showDeleteConfirm = false)
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            uiState = uiState.copy(deleteLoading = true)
            try {
                repository.deleteResource(uiState.resourceId)
                uiState = uiState.copy(
                    deleteLoading = false,
                    showDeleteConfirm = false,
                    deleteSuccess = true
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    deleteLoading = false,
                    showDeleteConfirm = false,
                    error = "Could not delete: ${e.localizedMessage}"
                )
            }
        }
    }

    // ── Offer ───────────────────────────────────────────────
    fun onShowOfferDialog() {
        uiState = uiState.copy(showOfferDialog = true, offerPrice = "", offerError = null)
    }

    fun onDismissOfferDialog() {
        uiState = uiState.copy(showOfferDialog = false, offerPrice = "", offerError = null)
    }

    fun onOfferPriceChange(value: String) {
        uiState = uiState.copy(offerPrice = value, offerError = null)
    }

    fun onSubmitOffer() {
        val price = uiState.offerPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            uiState = uiState.copy(offerError = "Enter a valid price")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            messagesRepository.createConversation(
                resourceId = uiState.resourceId,
                resourceTitle = uiState.title,
                sellerId = uiState.sellerId
            ).onSuccess { conversation ->
                messagesRepository.sendOffer(
                    conversationId = conversation.id,
                    offerPrice = price
                ).onSuccess {
                    uiState = uiState.copy(isLoading = false, showOfferDialog = false)
                    _navigationEvent.send(
                        ProductNavigationEvent.NavigateToConversation(
                            conversationId = conversation.id,
                            title = uiState.title,
                            otherUserAvatar = uiState.sellerAvatarUrl,
                            otherUserUsername = uiState.sellerUsername,
                            otherUserDisplayName = uiState.sellerName,
                        )
                    )
                }.onFailure {
                    uiState = uiState.copy(isLoading = false, offerError = "Failed to send offer")
                }
            }.onFailure {
                uiState = uiState.copy(isLoading = false, offerError = "Failed to open conversation")
            }
        }
    }

    fun onMessageSeller() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            messagesRepository.createConversation(
                resourceId = uiState.resourceId,
                resourceTitle = uiState.title,
                sellerId = uiState.sellerId
            ).onSuccess { conversation ->
                uiState = uiState.copy(isLoading = false)
                _navigationEvent.send(
                    ProductNavigationEvent.NavigateToConversation(
                        conversationId = conversation.id,
                        title = uiState.title,
                        otherUserAvatar = uiState.sellerAvatarUrl,
                        otherUserUsername = uiState.sellerUsername,
                        otherUserDisplayName = uiState.sellerName,
                    )
                )
            }.onFailure {
                uiState = uiState.copy(isLoading = false, error = "Could not open conversation")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProductViewModel(
                    repository = AppModule.resourceRepository,
                    messagesRepository = AppModule.messagesRepository
                )
            }
        }
    }
}

sealed class ProductNavigationEvent {
    data class NavigateToConversation(
        val conversationId: String,
        val title: String,
        val otherUserAvatar: String?,
        val otherUserUsername: String?,
        val otherUserDisplayName: String?,
    ) : ProductNavigationEvent()
}