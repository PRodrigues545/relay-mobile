package ipca.api.relay.presentation.reviews.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxStars: Int = 5,
    starSize: Int = 36,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(maxStars) { index ->
            val starIndex = index + 1
            val filled = rating >= starIndex
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = "$starIndex estrela${if (starIndex == 1) "" else "s"}",
                tint = Color(0xFFFFB800),
                modifier = Modifier
                    .size(starSize.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onRatingChange(starIndex) }
            )
        }
    }
}