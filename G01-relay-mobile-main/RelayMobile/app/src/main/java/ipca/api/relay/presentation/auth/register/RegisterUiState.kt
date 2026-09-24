package ipca.api.relay.presentation.auth.register

import com.google.android.libraries.places.api.model.AutocompletePrediction
import ipca.api.relay.domain.model.AddressPrediction
import ipca.api.relay.domain.model.Course
import ipca.api.relay.domain.model.University

data class RegisterUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val confirmPassword: String = "",
    val confirmPasswordError: String? = null,
    val showPassword: Boolean = false,
    val name: String = "",
    val nameError: String? = null,
    val username: String = "",
    val usernameError: String? = null,
    val dateOfBirthMillis: Long? = null,
    val university: String = "",
    val universityId: Int? = null,
    val universities: List<University> = emptyList(),
    val course: String = "",
    val courseId: Int? = null,
    val courses: List<Course> = emptyList(),
    val address: String = "",
    val addressPredictions: List<AddressPrediction> = emptyList(),
    val addressError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentStep : RegisterStep = RegisterStep.CREDENTIALS
)

enum class RegisterStep {
    CREDENTIALS,
    PERSONAL_INFO;

    fun next(): RegisterStep = when (this) {
        CREDENTIALS -> PERSONAL_INFO
        PERSONAL_INFO -> PERSONAL_INFO
    }

    fun previous(): RegisterStep = when (this) {
        CREDENTIALS -> CREDENTIALS
        PERSONAL_INFO -> CREDENTIALS
    }

    val isFirst get() = this == CREDENTIALS
    val isLast get() = this == PERSONAL_INFO
}