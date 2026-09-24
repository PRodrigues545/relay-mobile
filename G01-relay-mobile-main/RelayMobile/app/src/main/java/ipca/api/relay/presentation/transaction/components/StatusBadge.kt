package ipca.api.relay.presentation.transaction.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "COMPLETED" -> Color(0xFF2E7D32)
        "APPROVED", "ACTIVE" -> Color(0xFF1565C0)
        "REJECTED", "FAILED", "CANCELLED" -> Color(0xFFC62828)
        "AWAITING_SELLER", "AWAITING_OWNER" -> Color(0xFFE65100)
        else -> Color(0xFF6666FF)
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = statusLabel(status),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun statusLabel(status: String): String = when (status) {
    "REQUESTED" -> "Pedido"
    "AWAITING_SELLER" -> "Aguarda vendedor"
    "AWAITING_OWNER" -> "Aguarda dono"
    "APPROVED" -> "Aprovado"
    "ACTIVE" -> "Ativo"
    "REJECTED" -> "Rejeitado"
    "CANCELLED" -> "Cancelado"
    "RETURNED" -> "Devolvido"
    "COMPLETED" -> "Completed"
    "FAILED" -> "Falhado"
    else -> status
}