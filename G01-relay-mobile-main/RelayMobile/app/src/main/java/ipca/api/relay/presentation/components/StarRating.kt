package ipca.api.relay.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    rating: Float,
    maxStars: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            val starIndex = index + 1
            val icon = when {
                rating >= starIndex -> Icons.Rounded.Star // Full star
                rating >= starIndex - 0.5f -> Icons.Rounded.StarHalf // Half star
                else -> Icons.Rounded.StarOutline // Empty star
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFB800), // Classic Star Gold
                modifier = Modifier.size(18.dp)
            )
        }
    }
}