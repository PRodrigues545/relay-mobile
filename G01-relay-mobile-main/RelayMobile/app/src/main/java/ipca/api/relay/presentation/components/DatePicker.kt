package ipca.api.relay.presentation.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import ipca.api.relay.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatePickerDocked(
    selectedDateMillis : Long?,
    onDateChange : (Long?) -> Unit,
    modifier: Modifier = Modifier
){
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )
    val selectedDate = selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    Column(

    ){
        Text(
            text = "Date of Birth",
            color = Color(0xFF52527A)
        )

        Spacer(
            Modifier.height(4.dp)
        )

        Box(
            modifier = modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Date of Birth") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = !showDatePicker }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Date Picker"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )

            if (showDatePicker) {
                Popup(
                    onDismissRequest = {
                        onDateChange(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    },
                    alignment = Alignment.TopStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = 64.dp)
                            .shadow(elevation = 4.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Column {
                            DatePicker(state = datePickerState, showModeToggle = false, title = {
                                Row() {
                                    Text(
                                        text = "Date of Birth",
                                        fontSize = 24.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            .weight(1f).align(Alignment.CenterVertically),
                                    )
                                    // Optional confirm button for better UX
                                    Button(
                                        onClick = {
                                            onDateChange(datePickerState.selectedDateMillis)
                                            showDatePicker = false
                                        },
                                        modifier = Modifier.padding(end = 8.dp, top = 8.dp)
                                    ) {
                                        Text("OK")
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}


fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}