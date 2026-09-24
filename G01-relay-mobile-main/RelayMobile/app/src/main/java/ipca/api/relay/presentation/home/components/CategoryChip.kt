package ipca.api.relay.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryChip(
    category: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    val selectedColor = Color(0xFF6666FF)
    val unselectedBorder = Color(0xFFACACAC)
    val unselectedText = Color(0xFFACACAC)

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                color = if (selected) selectedColor else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (selected) selectedColor else unselectedBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        Text(
            text = category,
            color = if (selected) Color.White else unselectedText
        )
    }
}