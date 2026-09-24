package ipca.api.relay.presentation.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AuthLabeledInput(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
){
    Column(
        modifier = modifier
    ){
        Text(
            text = label,
            color = Color(0xFF52527A)
        )

        Spacer(
            Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = value,
            placeholder = { Text(
                text = placeholder,
                color = Color(0xFF9494D1),
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                lineHeight = 14.sp
            ) },
            onValueChange = onValueChange,
            singleLine = true,
            isError = error != null,
            supportingText = if (error != null) {
                { Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
