package ipca.api.relay.presentation.auth.register

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import ipca.api.relay.data.repository.AuthException
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.AddressPrediction
import ipca.api.relay.domain.model.Course
import ipca.api.relay.domain.model.University
import ipca.api.relay.domain.repository.AuthRepository
import ipca.api.relay.domain.repository.PlacesRepository
import ipca.api.relay.domain.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repo: AuthRepository,
    private val userRepo: UserRepository,
    private val placesRepo: PlacesRepository
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    var imageUri by mutableStateOf<Uri?>(null)
        private set

    private val _navigationEvent = Channel<RegisterNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Session token lives here — groups requests for billing
    private val addressSessionToken = AutocompleteSessionToken.newInstance()

    // ── image ──────────────────────────────────────────────────

    fun onImageSelected(uri: Uri?) { imageUri = uri }

    // ── step navigation ────────────────────────────────────────

    fun onStepOneContinue() {
        val emailError = when {
            uiState.email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches() -> "Invalid email"
            else -> null
        }
        val passwordError = when {
            uiState.password.isBlank() -> "Password is required"
            uiState.password.length < 8 -> "At least 8 characters"
            else -> null
        }
        val confirmPasswordError = when {
            uiState.confirmPassword.isBlank() -> "Required"
            uiState.confirmPassword != uiState.password -> "Passwords do not match"
            else -> null
        }
        val usernameError = when {
            uiState.username.isBlank() -> "Username is required"
            uiState.username.length < 3 -> "At least 3 characters"
            uiState.username.contains(" ") -> "No spaces allowed"
            else -> null
        }

        if (emailError != null || passwordError != null || confirmPasswordError != null || usernameError != null) {
            uiState = uiState.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                usernameError = usernameError
            )
            return
        }

        uiState = uiState.copy(
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            usernameError = null
        )
        uiState = uiState.copy(currentStep = uiState.currentStep.next())
        loadUniversities()
    }

    private fun loadUniversities() {
        viewModelScope.launch {
            userRepo.getUniversities()
                .onSuccess { uiState = uiState.copy(universities = it) }
        }
    }

    fun onUniversitySelected(university: University) {
        uiState = uiState.copy(
            university = university.name,
            universityId = university.id,
            course = "",         // reset course when university changes
            courseId = null,
            courses = emptyList()
        )
        viewModelScope.launch {
            userRepo.getCoursesByUniversity(university.id)
                .onSuccess { uiState = uiState.copy(courses = it) }
        }
    }

    fun onCourseSelected(course: Course) {
        uiState = uiState.copy(
            course = course.name,
            courseId = course.id
        )
    }

    fun previousStep() {
        uiState = uiState.copy(currentStep = uiState.currentStep.previous())
    }

    // ── field changes ──────────────────────────────────────────

    fun onEmailChange(email: String) { uiState = uiState.copy(email = email) }
    fun onPasswordChange(password: String) { uiState = uiState.copy(password = password) }
    fun onConfirmPasswordChange(confirmPassword: String) { uiState = uiState.copy(confirmPassword = confirmPassword) }
    fun onPasswordVisibilityToggle() { uiState = uiState.copy(showPassword = !uiState.showPassword) }
    fun onNameChange(name: String) { uiState = uiState.copy(name = name) }
    fun onUsernameChange(username: String) { uiState = uiState.copy(username = username) }
    fun onDateChange(dateMillis: Long?) { uiState = uiState.copy(dateOfBirthMillis = dateMillis) }
    fun onAddressChange(query: String) {
        uiState = uiState.copy(address = query)
        if (query.length >= 3) {
            viewModelScope.launch {
                val results = runCatching {
                    placesRepo.getAutocompletePredictions(query, addressSessionToken)
                        .map { prediction ->
                            AddressPrediction(
                                placeId = prediction.placeId,
                                primaryText = prediction.getPrimaryText(null).toString(),
                                secondaryText = prediction.getSecondaryText(null).toString(),
                                fullText = prediction.getFullText(null).toString()
                            )
                        }
                }.getOrDefault(emptyList())
                uiState = uiState.copy(addressPredictions = results)
            }
        } else {
            uiState = uiState.copy(addressPredictions = emptyList())
        }
    }

    fun onAddressSelected(prediction: AddressPrediction) {
        uiState = uiState.copy(
            address = prediction.fullText,
            addressPredictions = emptyList()
        )
    }

    // ── submit ─────────────────────────────────────────────────

    // called by both "Register" and "Skip" — skip just doesn't fill optional fields
    fun onRegisterClick() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repo.register(
                username = uiState.username,
                email = uiState.email.trim(),
                password = uiState.password
            )
                .onSuccess {

                    AppModule.profileLocalStore.saveUser(
                        name = uiState.name,
                        email = uiState.email.trim(),
                        username = uiState.username
                    )

                    AppModule.userRepository.updateMe(
                        name = uiState.name.takeIf { it.isNotBlank() },
                        address = uiState.address.takeIf { it.isNotBlank() },
                        courseId = uiState.courseId,
                        universityId = uiState.universityId
                    ).onFailure {
                        // profile update failing silently is bad
                        Log.w("RegisterViewModel", "Profile update after register failed: ${it.message}")
                    }

                    imageUri?.let { uri ->
                        userRepo.uploadAvatar(uri)
                    }

                    _navigationEvent.send(RegisterNavigationEvent.NavigateToHome)
                }
                .onFailure { error ->
                    val message = when (error) {
                        is AuthException.UserAlreadyExists -> "Email already in use"
                        is AuthException.NetworkError -> "No internet connection"
                        else -> "Something went wrong"
                    }
                    uiState = uiState.copy(error = message)
                }
            uiState = uiState.copy(isLoading = false)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RegisterViewModel(
                    repo = AppModule.authRepository,
                    userRepo= AppModule.userRepository,
                    placesRepo = AppModule.placesRepository
                )
            }
        }
    }
}

sealed interface RegisterNavigationEvent {
    data object NavigateToHome : RegisterNavigationEvent
}