package ipca.api.relay.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MenuOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {

    val primary = Color(0xFF6666FF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = Color(0xFFF5F5FF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {

        Column {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}