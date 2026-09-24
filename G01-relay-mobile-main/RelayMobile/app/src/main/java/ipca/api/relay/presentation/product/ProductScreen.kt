package ipca.api.relay.presentation.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ipca.api.relay.presentation.product.components.OfferDialog
import ipca.api.relay.presentation.components.StarRating

// The bottom bar's intrinsic height is roughly button height (56.dp) plus the
// 16.dp vertical padding on each side — 88.dp. We render a Surface of this
// exact size while loading so the screen layout doesn't shift the moment the
// real bar resolves. Magic number is fine; it's locked to MaterialButton size.
private val BOTTOM_BAR_HEIGHT = 88.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    id: Int,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onPurchaseClick: (resourceId: Int) -> Unit,
    onBorrowClick: (resourceId: Int) -> Unit,
    onNavigateToConversation: (
        conversationId: String,
        title: String,
        otherUserAvatar: String?,
        otherUserUsername: String?,
        otherUserDisplayName: String?,
    ) -> Unit,
    onSellerClick: (sellerId: Int) -> Unit = {},
    /**
     * When the caller (e.g. Home, Profile listings) already knows who owns
     * the resource, passing it here lets ProductScreen render the correct
     * bottom bar — Owner vs Sell vs Borrow — on the very first frame. Without
     * a hint, the screen waits until either the local cache or the network
     * resolves the owner before showing any bottom bar at all.
     */
    ownerIdHint: Int? = null,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory),
    modifier: Modifier = Modifier
) {
    val state = viewModel.uiState

    // Reload on every resume so edits made in EditResource land back here
    // as fresh state. LaunchedEffect(id) alone fires only once because the
    // id doesn't change when popping back from edit.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, id) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadResource(id, ownerIdHint)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) onBackClick()
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ProductNavigationEvent.NavigateToConversation -> {
                    onNavigateToConversation(
                        event.conversationId,
                        event.title,
                        event.otherUserAvatar,
                        event.otherUserUsername,
                        event.otherUserDisplayName,
                    )
                }
            }
        }
    }

    if (state.showOfferDialog) {
        OfferDialog(
            offerPrice = state.offerPrice,
            onOfferPriceChange = viewModel::onOfferPriceChange,
            offerError = state.offerError,
            isLoading = state.offerLoading,
            onDismiss = viewModel::onDismissOfferDialog,
            onConfirm = viewModel::onSubmitOffer
        )
    }

    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDelete,
            title = { Text("Delete Listing") },
            text = { Text("Are you sure you want to delete \"${state.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onConfirmDelete,
                    enabled = !state.deleteLoading
                ) {
                    if (state.deleteLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDelete) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Render a height-stable placeholder until we know which bar to
            // show. The Scaffold subtracts the bottomBar's height from the
            // content padding, so reserving the final size here prevents the
            // content from jumping when the real bar paints. The placeholder
            // also matches the real bar's tonalElevation so the background
            // tint doesn't visibly change.
            if (!state.hasLoaded) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BOTTOM_BAR_HEIGHT)
                ) { /* intentionally empty — pure layout reservation */ }
            } else {
                when {
                    // Owners get a quieter footer with edit + delete laid out
                    // as a pair. No "Buy now" / "Borrow" for your own publication.
                    state.isOwner -> OwnerBottomBar(
                        onEditClick = onEditClick,
                        onDeleteClick = viewModel::onRequestDelete
                    )
                    state.type == "BORROW" -> BorrowBottomBar(onBorrowClick = { onBorrowClick(id) })
                    else -> SellBottomBar(
                        price = state.price,
                        onBuyNowClick = { onPurchaseClick(id) },
                        onOfferClick = viewModel::onShowOfferDialog
                    )
                }
            }
        }
    ) { paddingValues ->
        ProductContent(
            state = state,
            onSellerClick = onSellerClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun ProductContent(
    state: ProductUiState,
    onSellerClick: (sellerId: Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            if (state.imageUrl != null) {
                AsyncImage(
                    model = state.imageUrl,
                    contentDescription = state.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
                    tint = Color.LightGray
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (state.type != "BORROW") {
                Text(
                    text = "${state.price}€",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (state.condition != null) {
                Text(
                    text = "Estado: ${conditionLabel(state.condition)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (state.type == "BORROW" || state.type == "BOTH") {
                if (state.borrowDuration != null) {
                    Text(
                        text = "Duration: ${state.borrowDuration}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (state.zones != null) {
                    Text(
                        text = "Zones: ${state.zones}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Seller block — hidden for owner (no point in linking to your
            // own public profile from your own listing). Also hidden until
            // hasLoaded is true so an empty/avatar-less stub doesn't flash
            // before the real seller data arrives.
            if (state.hasLoaded && !state.isOwner) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSellerClick(state.sellerId) }
                        .padding(vertical = 4.dp)
                ) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        AsyncImage(
                            model = state.sellerAvatarUrl,
                            contentDescription = "Seller's avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = state.sellerName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StarRating(rating = state.sellerReputation)

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "(${String.format("%.1f", state.sellerReputation)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SellBottomBar(
    price: Float,
    onBuyNowClick: () -> Unit,
    onOfferClick: () -> Unit
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onOfferClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Offer")
            }
            Button(
                onClick = onBuyNowClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Buy now")
            }
        }
    }
}

@Composable
fun BorrowBottomBar(onBorrowClick: () -> Unit) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Button(
            onClick = onBorrowClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Ask for loan")
        }
    }
}

@Composable
fun OwnerBottomBar(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Edit")
            }
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete")
            }
        }
    }
}

private fun conditionLabel(condition: String): String = when (condition) {
    "NEW" -> "New"
    "LIKE_NEW" -> "Like new"
    "GOOD" -> "Good"
    "FAIR" -> "Fair condition"
    "POOR" -> "Poor"
    else -> condition
}