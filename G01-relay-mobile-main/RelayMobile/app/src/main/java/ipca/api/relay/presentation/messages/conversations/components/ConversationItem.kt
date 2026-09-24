package ipca.api.relay.presentation.messages.conversations.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ipca.api.relay.domain.model.Conversation

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    val headline = conversation.otherUserDisplayName
        ?: conversation.otherUserUsername
        ?: conversation.resourceTitle
    val avatarFallbackInitial =
        conversation.otherUserDisplayName?.firstOrNull()
            ?: conversation.otherUserUsername?.firstOrNull()
            ?: conversation.resourceTitle.firstOrNull()
            ?: '?'

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conversation.otherUserAvatar != null) {
            AsyncImage(
                model = conversation.otherUserAvatar,
                contentDescription = headline,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF6666FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarFallbackInitial.toString().uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = headline,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            if (headline != conversation.resourceTitle) {
                Text(
                    text = conversation.resourceTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}
