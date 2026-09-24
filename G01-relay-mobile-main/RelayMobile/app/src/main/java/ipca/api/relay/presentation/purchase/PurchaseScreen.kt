package ipca.api.relay.presentation.purchase

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    resourceId: Int,
    onBack: () -> Unit,
    onPickMeetingPoint: () -> Unit,
    onPickPaymentMethod: () -> Unit,
    onFinished: (purchaseId: Int) -> Unit,
    viewModel: PurchaseViewModel = viewModel(factory = PurchaseViewModel.factory(resourceId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val primary = Color(0xFF6666FF)

    // Stripe PaymentSheet
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed ->
                onFinished(state.createdPurchaseId!!)
            is PaymentSheetResult.Failed ->
                viewModel.onPaymentError(result.error.message)
            is PaymentSheetResult.Canceled -> { /* utilizador cancelou */ }
        }
    }

    // Once the backend confirms the purchase request, hand control to the
    // parent — typically navigating to the transaction detail screen.
    // Lança o sheet quando o clientSecret chegar
    // Navega direto quando não é Stripe (CASH ou MBWAY)
    LaunchedEffect(state.createdPurchaseId, state.clientSecret) {
        val id = state.createdPurchaseId
        if (id != null && state.clientSecret == null) {
            onFinished(id)
        }
    }

// Abre PaymentSheet só para BANK_TRANSFER
    LaunchedEffect(state.clientSecret) {
        state.clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                secret,
                PaymentSheet.Configuration(merchantDisplayName = "Relay")
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purchase") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            PurchaseFooter(
                total = state.price,
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
            ) {
                CircularProgressIndicator(color = primary)
            }

            state.error != null -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 24.dp)
            ) {
                ProductHeader(
                    imageUrl = state.imageUrl,
                    title = state.title,
                    conditionLabel = state.conditionLabel,
                    price = state.price,
                    primary = primary,
                )

                Spacer(Modifier.height(24.dp))

                SectionLabel("Meeting Point", primary)
                Spacer(Modifier.height(8.dp))
                PickerRow(
                    text = state.selectedMeetingPoint?.name ?: "Select a pickup point",
                    onClick = onPickMeetingPoint,
                    primary = primary,
                )

                Spacer(Modifier.height(20.dp))

                SectionLabel("Payment Method", primary)
                Spacer(Modifier.height(8.dp))
                PickerRow(
                    text = state.selectedPaymentMethod?.label ?: "Choose a payment method",
                    onClick = onPickPaymentMethod,
                    primary = primary,
                )

                state.submitError?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProductHeader(
    imageUrl: String?,
    title: String,
    conditionLabel: String,
    price: Double,
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
                text = title,
                color = primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            if (conditionLabel.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(conditionLabel, fontSize = 14.sp, color = Color.Black)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Price: ${formatEuros(price)}",
                fontSize = 14.sp,
                color = Color.Black
            )
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
private fun PickerRow(
    text: String,
    onClick: () -> Unit,
    primary: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, primary, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 14.sp, color = Color.Black)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = primary)
    }
}

@Composable
private fun PurchaseFooter(
    total: Double,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    primary: Color,
    onSubmit: () -> Unit,
) {
    Surface(
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total to pay", fontSize = 14.sp, color = Color.Black)
                Text(
                    formatEuros(total),
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
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
                    Text("Buy", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun formatEuros(amount: Double): String =
    "%.2f €".format(amount).replace('.', ',')