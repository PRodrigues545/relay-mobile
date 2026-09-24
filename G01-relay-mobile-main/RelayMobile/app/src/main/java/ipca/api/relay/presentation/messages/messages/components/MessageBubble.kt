package ipca.api.relay.presentation.messages.messages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ipca.api.relay.domain.model.Message

@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    otherUserAvatar: String? = null,
    otherUserUsername: String? = null,
    currentUserId: Int = -1,
    onAcceptOffer: () -> Unit = {},
    onDeclineOffer: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwn) {
            if (otherUserAvatar != null) {
                AsyncImage(
                    model = otherUserAvatar,
                    contentDescription = otherUserUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF6666FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = otherUserUsername?.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        if (message.type == "COUNTER_OFFER") {
            OfferBubble(
                message = message,
                isOwn = isOwn,
                canRespond = message.offerStatus == "PENDING" && !isOwn,
                onAccept = onAcceptOffer,
                onDecline = onDeclineOffer
            )
        } else {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (isOwn) 16.dp else 4.dp,
                    bottomEnd = if (isOwn) 4.dp else 16.dp
                ),
                color = if (isOwn) Color(0xFF6666FF) else Color(0xFFF0F0F0),
                modifier = Modifier.widthIn(max = 260.dp)
            ) {
                Text(
                    text = message.body,
                    color = if (isOwn) Color.White else Color.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (isOwn) {
            Spacer(Modifier.width(40.dp))
        }
    }
}