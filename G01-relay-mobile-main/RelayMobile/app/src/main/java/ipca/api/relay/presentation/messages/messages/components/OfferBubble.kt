package ipca.api.relay.presentation.messages.messages.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.domain.model.Message

@Composable
fun OfferBubble(
    message: Message,
    isOwn: Boolean,
    canRespond: Boolean = false,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3E0),
        border = BorderStroke(1.dp, Color(0xFFFF9800)),
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "💰 Offer",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
            Text(
                "€${message.offerPrice}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            when (message.offerStatus) {
                "ACCEPTED" -> Text(
                    "✅ Accepted",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
                "DECLINED" -> Text(
                    "❌ Rejected",
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )
                else -> {
                    if (canRespond) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onDecline,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFFC62828)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFC62828)
                                )
                            ) {
                                Text("Recusar", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32)
                                )
                            ) {
                                Text("Aceitar", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            "⏳ Awaiting response...",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}