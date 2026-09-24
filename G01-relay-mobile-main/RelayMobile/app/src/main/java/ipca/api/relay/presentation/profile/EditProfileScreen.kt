package ipca.api.relay.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import ipca.api.relay.presentation.auth.components.AuthLabeledInput
import ipca.api.relay.presentation.auth.register.components.SelectDropdownField
import ipca.api.relay.presentation.components.AddressAutocompleteField
import ipca.api.relay.presentation.components.PrimaryButton
import ipca.api.relay.presentation.components.RoundImage

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val user = viewModel.user
    val draft = viewModel.editDraft

    // Lazy-load universities (and courses for the current one) on entry.
    LaunchedEffect(Unit) {
        viewModel.loadUniversitiesIfNeeded()
    }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) viewModel.uploadAvatar(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        RelayTopBar(
            title = "Edit Profile",
            onBackClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Avatar ──
            Spacer(Modifier.height(16.dp))

            if (user.profilePicture != null) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .clickable { avatarPickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Change Photo",
                    color = RelayPurple,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { avatarPickerLauncher.launch("image/*") }
                )
            } else {
                RoundImage(
                    image = rememberAsyncImagePainter(model = null),
                    onClick = { avatarPickerLauncher.launch("image/*") },
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Add Photo",
                    color = RelayPurple,
                    fontSize = 13.sp
                )
            }

            if (viewModel.isLoading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = RelayPurple
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Display name ──
            AuthLabeledInput(
                label = "Display Name",
                value = draft.name,
                onValueChange = viewModel::onDraftNameChange,
                placeholder = "How you want to be displayed"
            )

            Spacer(Modifier.height(16.dp))

            // ── University ──
            SelectDropdownField(
                label = "University",
                placeholder = if (draft.universitiesLoading) "Loading…" else "Choose your university",
                selectedText = draft.university,
                options = draft.universities,
                optionLabel = { it.name },
                onOptionSelected = viewModel::onDraftUniversitySelected,
                enabled = !draft.universitiesLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ── Course ──
            SelectDropdownField(
                label = "Course",
                placeholder = if (draft.universityId == null) "Choose a university first" else "Choose your course",
                selectedText = draft.course,
                options = draft.courses,
                optionLabel = { it.name },
                onOptionSelected = viewModel::onDraftCourseSelected,
                enabled = draft.universityId != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ── Address ──
            AddressAutocompleteField(
                label = "Location",
                placeholder = "City or town",
                value = draft.address,
                predictions = draft.addressPredictions,
                onValueChange = viewModel::onDraftAddressChange,
                onPredictionSelected = viewModel::onDraftAddressSelected
            )

            viewModel.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = if (viewModel.isLoading) "Saving…" else "Save",
                onClick = {
                    viewModel.saveEditProfile {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}