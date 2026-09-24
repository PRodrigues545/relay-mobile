package ipca.api.relay.presentation.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.domain.model.Transaction
import ipca.api.relay.domain.model.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    val primary = Color(0xFF6666FF)
    val statusColor = statusColor(transaction.statusName)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == TransactionType.PURCHASE)
                        Icons.Default.ShoppingCart else Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Resource #${transaction.resourceId}",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (transaction.isBuyer) "Purchased" else "Sold",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (transaction.agreedPrice != null) {
                    Text(
                        text = "€${transaction.agreedPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusLabel(transaction.statusName),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun statusColor(status: String): Color = when (status) {
    "COMPLETED" -> Color(0xFF2E7D32)
    "APPROVED", "ACTIVE" -> Color(0xFF1565C0)
    "REJECTED", "FAILED", "CANCELLED" -> Color(0xFFC62828)
    "AWAITING_SELLER", "AWAITING_OWNER" -> Color(0xFFE65100)
    else -> Color(0xFF6666FF)
}

private fun statusLabel(status: String): String = when (status) {
    "REQUESTED" -> "Requested"
    "AWAITING_SELLER" -> "Awaiting Seller"
    "AWAITING_OWNER" -> "Awaiting Owner"
    "APPROVED" -> "Approved"
    "ACTIVE" -> "Active"
    "REJECTED" -> "Rejected"
    "CANCELLED" -> "Cancelled"
    "RETURNED" -> "Returned"
    "COMPLETED" -> "Completed"
    "FAILED" -> "Failed"
    else -> status
}