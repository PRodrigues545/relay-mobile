package ipca.api.relay.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    onClick : () -> Unit,
    text : String,
    enabled : Boolean = true
){
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp), // or RectangleShape
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFCECEEB),
            contentColor = Color(0xFF6666FF)
        ),
        border = BorderStroke(1.dp, Color(0xFF6666FF)),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 16.sp
        )
    }
}