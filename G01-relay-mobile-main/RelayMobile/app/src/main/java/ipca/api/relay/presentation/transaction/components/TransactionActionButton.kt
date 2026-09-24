package ipca.api.relay.presentation.transaction.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ipca.api.relay.presentation.transaction.TransactionAction

@Composable
fun TransactionActionButton(
    action: TransactionAction,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val (label, color, outlined) = when (action) {
        TransactionAction.APPROVE -> Triple("Aprovar", Color(0xFF2E7D32), false)
        TransactionAction.REJECT -> Triple("Rejeitar", Color(0xFFC62828), true)
        TransactionAction.CANCEL -> Triple("Cancelar", Color(0xFFC62828), true)
        TransactionAction.COMPLETE -> Triple("Confirm Receipt", Color(0xFF6666FF), false)
        TransactionAction.RETURN -> Triple("Marcar como Devolvido", Color(0xFF6666FF), false)
    }

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, color),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Text(label, fontWeight = FontWeight.Medium)
            }
        }
    } else {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = color)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(label, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}