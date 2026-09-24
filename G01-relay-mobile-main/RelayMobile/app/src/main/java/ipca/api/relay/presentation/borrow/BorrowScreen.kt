package ipca.api.relay.presentation.borrow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowScreen(
    resourceId: Int,
    onBack: () -> Unit,
    onPickMeetingPoint: () -> Unit,
    onFinished: (borrowId: Int) -> Unit,
    viewModel: BorrowViewModel = viewModel(factory = BorrowViewModel.factory(resourceId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val primary = Color(0xFF6666FF)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.createdBorrowId) {
        state.createdBorrowId?.let(onFinished)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borrow Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            BorrowFooter(
                duration = state.borrowDuration,
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                primary = primary,
                onSubmit = viewModel::onSubmit
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = primary) }

            state.error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(state.error!!, color = MaterialTheme.colorScheme.error) }

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                BorrowHeader(
                    imageUrl = state.imageUrl,
                    title = state.title,
                    descriptionShort = state.descriptionShort,
                    durationLabel = state.borrowDuration,
                    primary = primary,
                )

                if (state.zones.isNotBlank()) {
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("Zones", primary)
                    Spacer(Modifier.height(4.dp))
                    Text(state.zones, fontSize = 14.sp, color = Color.Black)
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel("Start", primary)
                        Spacer(Modifier.height(8.dp))
                        DatePickerRow(
                            text = state.startDate.ifBlank { "Select" },
                            primary = primary,
                            onClick = { showStartPicker = true }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel("End", primary)
                        Spacer(Modifier.height(8.dp))
                        DatePickerRow(
                            text = state.endDate.ifBlank { "Select" },
                            primary = primary,
                            onClick = { showEndPicker = true }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                SectionLabel("Meeting Point", primary)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, primary, RoundedCornerShape(12.dp))
                        .clickable(onClick = onPickMeetingPoint)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state.selectedMeetingPoint?.name ?: "Select a pickup point",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = primary)
                }

                state.submitError?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }

    if (showStartPicker) {
        IsoDatePickerDialog(
            initial = state.startDate.takeIf { it.isNotBlank() } ?: LocalDate.now().toString(),
            onConfirm = {
                viewModel.onStartDateChange(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }
    if (showEndPicker) {
        IsoDatePickerDialog(
            initial = state.endDate.takeIf { it.isNotBlank() }
                ?: state.startDate.takeIf { it.isNotBlank() }
                ?: LocalDate.now().toString(),
            // Server requires end > start; constrain selectable dates here so
            // the user can't even pick an invalid range.
            minDateInclusive = state.startDate.takeIf { it.isNotBlank() }?.let {
                runCatching { LocalDate.parse(it).plusDays(1) }.getOrNull()
            },
            onConfirm = {
                viewModel.onEndDateChange(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun BorrowHeader(
    imageUrl: String?,
    title: String,
    descriptionShort: String,
    durationLabel: String,
    primary: Color,
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(1.dp, primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            if (descriptionShort.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    descriptionShort.take(80),
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 2
                )
            }
            if (durationLabel.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("Duration: $durationLabel", fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, primary: Color) {
    Text(
        text = text,
        color = primary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
}

@Composable
private fun DatePickerRow(text: String, primary: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primary, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 13.sp, color = Color.Black)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = primary)
    }
}

@Composable
private fun BorrowFooter(
    duration: String,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    primary: Color,
    onSubmit: () -> Unit,
) {
    Surface(color = Color.White, tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (duration.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Duration", fontSize = 14.sp, color = Color.Black)
                    Text(duration, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
            }
            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Submit Request", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IsoDatePickerDialog(
    initial: String,
    minDateInclusive: LocalDate? = null,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = runCatching {
        LocalDate.parse(initial).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }.getOrDefault(System.currentTimeMillis())

    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val min = minDateInclusive ?: return true
                val minMillis = min.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                return utcTimeMillis >= minMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) {
                    val iso = Instant.ofEpochMilli(millis)
                        .atOffset(ZoneOffset.UTC)
                        .toLocalDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)
                    onConfirm(iso)
                } else {
                    onDismiss()
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = state)
    }
}