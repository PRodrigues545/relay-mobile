package ipca.api.relay.presentation.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.domain.model.TransactionType
import ipca.api.relay.presentation.transaction.components.DetailRow
import ipca.api.relay.presentation.transaction.components.StatusBadge
import ipca.api.relay.presentation.transaction.components.TransactionActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    id: Int,
    type: TransactionType,
    onBack: () -> Unit,
    // Invoked when the user taps "Avaliar". The graph supplies the navigation;
    // the screen itself doesn't know who the other party is by name (the DTOs
    // only carry IDs), so it passes empty/null defaults — WriteReviewScreen
    // shows a sensible fallback header in that case.
    onWriteReview: (reviewedName: String, reviewedPicture: String?) -> Unit = { _, _ -> },
    // Set by the graph when WriteReviewScreen reports success (via
    // savedStateHandle). Flips the Avaliar CTA to a non-tappable "✓ Avaliado"
    // indicator. Intentionally not persisted across screen re-entries — a
    // fresh entry re-shows Avaliar; the server is the source of truth for
    // duplicates and surfaces them as a WriteReviewScreen error.
    reviewedInSession: Boolean = false,
    modifier: Modifier = Modifier,
    viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModel.factory(id, type)
    )
) {
    val state = viewModel.uiState
    val primary = Color(0xFF6666FF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (type == TransactionType.PURCHASE) "Purchase" else "Loan")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = viewModel::loadDetail) { Text("Retry") }
                }
            }

            else -> Column(
                modifier = modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (type == TransactionType.PURCHASE)
                                    (if (state.isBuyer) "Purchase" else "Sale")
                                else
                                    (if (state.isBuyer) "Loan Requested" else "Item Loaned"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            StatusBadge(status = state.statusName)
                        }

                        Spacer(Modifier.height(12.dp))

                        DetailRow("Resource", "#${state.resourceId}")

                        if (state.agreedPrice != null) {
                            DetailRow("Agreed Price", "€${state.agreedPrice}")
                        }

                        if (type == TransactionType.BORROW) {
                            if (state.startDate != null) {
                                DetailRow("Start", formatDate(state.startDate))
                            }
                            if (state.endDate != null) {
                                DetailRow("End", formatDate(state.endDate))
                            }
                        }

                        DetailRow("Created at", formatDate(state.createdAt))
                    }
                }

                // Role indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (state.isBuyer)
                                Icons.Default.ShoppingCart
                            else
                                Icons.Default.Storefront,
                            contentDescription = null,
                            tint = primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                type == TransactionType.PURCHASE && state.isBuyer -> "You are the buyer"
                                type == TransactionType.PURCHASE -> "You are the seller"
                                state.isBuyer -> "You borrowed this"
                                else -> "You lent your item"
                            },
                            color = primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Success/error feedback
                state.actionSuccess?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                state.actionError?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Action buttons
                if (state.availableActions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.availableActions.forEach { action ->
                            TransactionActionButton(
                                action = action,
                                isLoading = state.actionLoading,
                                onClick = { viewModel.onAction(action) }
                            )
                        }
                    }
                }

                // Write-review CTA — shown once the transaction is fully done.
                // Sits below the (now empty) action list so the screen reads:
                // "transaction is complete, here is the followup".
                if (state.statusName == "COMPLETED") {
                    Spacer(Modifier.height(8.dp))
                    if (reviewedInSession) {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                disabledContainerColor = primary.copy(alpha = 0.15f),
                                disabledContentColor = primary
                            )
                        ) {
                            Text("✓ Reviewed", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                // We don't know the counterparty's name/picture
                                // from the transaction DTOs — pass blanks; the
                                // review screen handles the fallback header.
                                onWriteReview("", null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = primary
                            )
                        ) {
                            Text("Review", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val input = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(input)
    } catch (e: Exception) {
        dateString
    }
}