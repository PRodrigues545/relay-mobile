package ipca.api.relay.presentation.purchase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.domain.model.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodPickerScreen(
    initial: PaymentMethod? = null,
    onBack: () -> Unit,
    onConfirm: (PaymentMethod) -> Unit,
) {
    var selected by remember { mutableStateOf(initial ?: PaymentMethod.BANK_TRANSFER) }
    val primary = Color(0xFF6666FF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Method") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(color = Color.White, tonalElevation = 8.dp, shadowElevation = 8.dp) {
                Button(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Confirm", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Select a payment method",
                color = primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )

            PaymentMethod.entries.forEach { method ->
                PaymentRow(
                    method = method,
                    selected = method == selected,
                    primary = primary,
                    onClick = { selected = method }
                )
            }
        }
    }
}

@Composable
private fun PaymentRow(
    method: PaymentMethod,
    selected: Boolean,
    primary: Color,
    onClick: () -> Unit,
) {
    // Selected option gets a subtle tinted background to match the mock.
    val bg = if (selected) primary.copy(alpha = 0.08f) else Color.White
    val border = if (selected) primary else Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(method.label, color = Color.Black, fontSize = 14.sp)
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = primary)
        )
    }
}