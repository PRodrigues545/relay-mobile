package ipca.api.relay.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.places.api.model.AutocompletePrediction
import ipca.api.relay.domain.model.AddressPrediction
import ipca.api.relay.presentation.auth.components.AuthLabeledInput

@Composable
fun AddressAutocompleteField(
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String,
    value: String,
    predictions: List<AddressPrediction>,
    onValueChange: (String) -> Unit,
    onPredictionSelected: (AddressPrediction) -> Unit,
    error: String? = null,
) {
    Column(modifier = modifier) {
        AuthLabeledInput(
            label = label,
            placeholder = placeholder,
            value = value,
            onValueChange = onValueChange,
            error = error
        )

        if (predictions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                    items(
                        items = predictions,
                        key = { it.placeId }  // ← stable key helps Compose diff the list
                    ) { prediction ->
                        ListItem(
                            headlineContent = { Text(prediction.primaryText) },
                            supportingContent = {
                                Text(
                                    prediction.secondaryText,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9494D1)
                                )
                            },
                            modifier = Modifier.clickable {
                                onPredictionSelected(prediction)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}