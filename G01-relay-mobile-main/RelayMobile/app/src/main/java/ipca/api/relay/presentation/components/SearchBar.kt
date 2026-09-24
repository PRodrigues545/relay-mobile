package ipca.api.relay.presentation.components

//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val figmaColor = Color(0xFF2A2A5E)

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search...", color = Color.Gray) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = figmaColor // Lupa com a cor 2A2A5E
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = figmaColor,     // Contorno 2A2A5E
            unfocusedBorderColor = figmaColor,   // Contorno 2A2A5E
            cursorColor = figmaColor,
            focusedTextColor = Color.Black,        // ← adiciona isto
            unfocusedTextColor = Color.Black,      // ← e isto
            focusedPlaceholderColor = Color.Gray,  // ← e isto
            unfocusedPlaceholderColor = Color.Gray

        ),
        singleLine = true
    )
}