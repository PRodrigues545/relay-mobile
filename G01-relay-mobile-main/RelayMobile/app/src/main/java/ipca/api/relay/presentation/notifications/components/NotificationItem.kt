package ipca.api.relay.presentation.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ipca.api.relay.domain.model.Notification

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val bgColor = if (notification.isRead) Color.White else Color(0xFFF0F0FF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    notificationIconColor(notification.type).copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = notificationIcon(notification.type),
                contentDescription = null,
                tint = notificationIconColor(notification.type),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.title,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!notification.isRead) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF6666FF), CircleShape)
            )
        }
    }
}

private fun notificationIcon(type: String): ImageVector = when (type) {
    "PURCHASE_REQUESTED", "PURCHASE_APPROVED",
    "PURCHASE_REJECTED", "PURCHASE_COMPLETED" -> Icons.Default.ShoppingCart
    "BORROW_REQUESTED", "BORROW_APPROVED",
    "BORROW_REJECTED", "BORROW_COMPLETED",
    "BORROW_RETURNED" -> Icons.Default.SwapHoriz
    "MESSAGE_SENT" -> Icons.Default.Message
    "REVIEW_RECEIVED" -> Icons.Default.Star
    "OFFER_ACCEPTED", "OFFER_DECLINED" -> Icons.Default.LocalOffer
    else -> Icons.Default.Notifications
}

private fun notificationIconColor(type: String): Color = when (type) {
    "PURCHASE_APPROVED", "BORROW_APPROVED",
    "OFFER_ACCEPTED", "BORROW_COMPLETED",
    "PURCHASE_COMPLETED" -> Color(0xFF2E7D32)
    "PURCHASE_REJECTED", "BORROW_REJECTED",
    "OFFER_DECLINED" -> Color(0xFFC62828)
    "REVIEW_RECEIVED" -> Color(0xFFFF9800)
    "MESSAGE_SENT" -> Color(0xFF6666FF)
    else -> Color(0xFF6666FF)
}