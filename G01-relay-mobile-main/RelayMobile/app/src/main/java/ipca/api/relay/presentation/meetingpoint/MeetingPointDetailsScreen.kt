package ipca.api.relay.presentation.meetingpoint

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val RelayPurple = Color(0xFF6666FF)
private val RelayDark = Color(0xFF2A2A5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingPointDetailsScreen(
    point: MeetingPointUi,
    onBackClick: () -> Unit,
    onConfirm: (point: MeetingPointUi, selectedTime: String) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(13) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayTime = remember(selectedHour, selectedMinute) {
        "%02d:%02d".format(selectedHour, selectedMinute)
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(Modifier.width(8.dp))
            Text("Meeting Point Details", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }

        Spacer(Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(point.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(point.address, fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(
                "%.5f, %.5f".format(point.latitude, point.longitude),
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider()

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text("Schedule", color = RelayDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .border(1.dp, RelayPurple, RoundedCornerShape(12.dp))
                    .clickable { showTimePicker = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = RelayPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(displayTime, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Change", color = RelayPurple, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.weight(1f))
        HorizontalDivider()

        Button(
            onClick = { onConfirm(point, displayTime) },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RelayPurple, contentColor = Color.White)
        ) {
            Text("Select this pickup location", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// TimePickerDialog stays exactly as you had it — unchanged.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.wrapContentSize().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select hour",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RelayDark,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                )
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF0F0FF),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = RelayDark,
                        selectorColor = RelayPurple,
                        containerColor = Color.White,
                        periodSelectorBorderColor = RelayPurple,
                        timeSelectorSelectedContainerColor = RelayPurple,
                        timeSelectorUnselectedContainerColor = Color(0xFFF0F0FF),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = RelayDark
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = RelayPurple, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
                        colors = ButtonDefaults.buttonColors(containerColor = RelayPurple)
                    ) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}