package ipca.api.relay.presentation.auth.register.components

import android.graphics.fonts.FontStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> SelectDropdownField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    selectedText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    enabled: Boolean = true,
    error: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(text = label, color = Color(0xFF52527A))
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF9494D1),
                    fontSize = 14.sp
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF52527A) else Color(0xFF9494D1),
                    modifier = Modifier.clickable(enabled = enabled) { expanded = !expanded }
                )
            },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            } else null,
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = !expanded },
            singleLine = true
        )

        if (expanded && options.isNotEmpty()) {  // ← gate on expanded
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(options) { option ->
                        ListItem(
                            headlineContent = { Text(optionLabel(option)) },
                            modifier = Modifier.clickable {
                                onOptionSelected(option)
                                expanded = false  // ← close on selection
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}