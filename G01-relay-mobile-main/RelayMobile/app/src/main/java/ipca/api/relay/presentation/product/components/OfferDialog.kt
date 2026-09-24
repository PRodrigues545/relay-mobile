package ipca.api.relay.presentation.product.components

import android.R.id.primary
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun OfferDialog(
    offerPrice: String,
    onOfferPriceChange: (String) -> Unit,
    offerError: String?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = Color(0xFF6666FF)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Offer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = "Enter the amount you wish to offer the seller.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = offerPrice,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) onOfferPriceChange(it) },
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(8.dp),
                    suffix = { Text("€") },
                    placeholder = { Text("0.00") },
                    isError = offerError != null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )

                if (offerError != null) {
                    Text(offerError, color = Color.Red, fontSize = 12.sp)
                }

                Row(
                    Modifier.fillMaxWidth(), Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Offer")
                        }
                    }
                }
            }
        }
    }
}