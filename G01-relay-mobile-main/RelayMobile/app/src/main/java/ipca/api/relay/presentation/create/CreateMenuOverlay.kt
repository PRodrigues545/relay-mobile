package ipca.api.relay.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateMenuOverlay(
    onDismiss: () -> Unit,
    onNavigateToResource: () -> Unit,
    onNavigateToRequest: () -> Unit
) {
    val figmaPrimary = Color(0xFF2A2A5E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .width(200.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.5.dp, figmaPrimary, RoundedCornerShape(12.dp))
        ) {
            // Option: Publish Item
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToResource() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Publish Item",
                    color = figmaPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = figmaPrimary.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Option: Make Request
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRequest() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Make Request",
                    color = figmaPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}