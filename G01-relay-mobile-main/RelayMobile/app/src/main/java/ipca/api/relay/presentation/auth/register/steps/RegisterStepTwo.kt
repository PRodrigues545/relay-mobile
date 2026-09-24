package ipca.api.relay.presentation.auth.register.steps

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.android.libraries.places.api.model.AutocompletePrediction
import ipca.api.relay.domain.model.AddressPrediction
import ipca.api.relay.domain.model.Course
import ipca.api.relay.domain.model.University
import ipca.api.relay.presentation.auth.components.AuthLabeledInput
import ipca.api.relay.presentation.auth.register.RegisterUiState
import ipca.api.relay.presentation.auth.register.components.SelectDropdownField
import ipca.api.relay.presentation.components.AddressAutocompleteField
import ipca.api.relay.presentation.components.Logo
import ipca.api.relay.presentation.components.PrimaryButton
import ipca.api.relay.presentation.components.RoundImage
import ipca.api.relay.presentation.components.SecondaryButton

@Composable
fun RegisterStepTwo(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onUniversitySelected: (University) -> Unit,
    onCourseSelected: (Course) -> Unit,
    onAddressChange: (String) -> Unit,
    onAddressSelected: (AddressPrediction) -> Unit,
    onRegisterClick: () -> Unit,
    onSkipClick: () -> Unit,
    onPreviousClick: () -> Unit,
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Logo(modifier = Modifier.size(128.dp))
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Set up your profile (optional)",
            fontSize = 13.sp,
            color = Color(0xFF9494D1)
        )

        Spacer(Modifier.height(24.dp))

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = onImageSelected
        )

        RoundImage(
            image = rememberAsyncImagePainter(model = imageUri),
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.size(96.dp)
        )


        Spacer(Modifier.height(16.dp))

        AuthLabeledInput(
            label = "Display name",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Enter the name you want to display"
        )
        /*
        Spacer(Modifier.height(16.dp))

        DatePickerDocked(
            selectedDateMillis = state.dateOfBirthMillis,
            onDateChange = onDateSelected,
            modifier = Modifier.fillMaxWidth()
        )*/

        Spacer(Modifier.height(16.dp))

        SelectDropdownField(
            label = "University",
            placeholder = "Select your university",
            selectedText = state.university,
            options = state.universities,
            optionLabel = { it.name },
            onOptionSelected = onUniversitySelected,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        SelectDropdownField(
            label = "Course",
            placeholder = if (state.universityId == null) "Select a university first" else "Select your course",
            selectedText = state.course,
            options = state.courses,
            optionLabel = { it.name },
            onOptionSelected = onCourseSelected,
            enabled = state.universityId != null,   // ← disabled until university picked
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        AddressAutocompleteField(
            label = "Address",
            placeholder = "Enter your city or town",
            value = state.address,
            predictions = state.addressPredictions,
            onValueChange = onAddressChange,
            onPredictionSelected = onAddressSelected,
            error = state.addressError
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            onClick = onRegisterClick,
            text = if (state.isLoading) "Registering..." else "Register",
            modifier = Modifier.height(48.dp).fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // skip and previous on the same row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButton(
                onClick = onPreviousClick,
                text = "Previous",
                modifier = Modifier.height(48.dp).weight(1f)
            )
            SecondaryButton(
                onClick = onSkipClick,
                text = "Skip",
                modifier = Modifier.height(48.dp).weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}