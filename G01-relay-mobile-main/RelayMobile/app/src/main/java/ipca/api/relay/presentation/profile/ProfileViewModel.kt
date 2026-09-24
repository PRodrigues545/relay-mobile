package ipca.api.relay.presentation.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.AddressPrediction
import ipca.api.relay.domain.model.Course
import ipca.api.relay.domain.model.Resource
import ipca.api.relay.domain.model.University
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(FlowPreview::class)
class ProfileViewModel : ViewModel() {

    var user by mutableStateOf(
        emptyProfileUser.copy(
            name = AppModule.profileLocalStore.getName(),
            email = AppModule.profileLocalStore.getEmail(),
            username = AppModule.profileLocalStore.getUsername()
        )
    )
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var notificationsEnabled by mutableStateOf(false)
        private set

    var showLocationInProfile by mutableStateOf(false)
        private set

    var userResources by mutableStateOf<List<Resource>>(emptyList())
        private set

    var userId: Int = 0
        private set

    /**
     * Number of reviews the user can still write. Loaded on init and refreshed
     * by [refreshPendingReviewCount] when Profile resumes, so submitting a
     * review elsewhere updates the badge here without a full reload.
     */
    var pendingReviewCount by mutableStateOf(0)
        private set

    // ── Edit-profile draft (Register-style: all fields editable, one save) ──
    var editDraft by mutableStateOf(EditProfileDraft())
        private set

    private val addressSessionToken = AutocompleteSessionToken.newInstance()

    // Address autocomplete queries flow through this StateFlow so they can be
    // debounced. Each keystroke previously fired a separate Places API call
    // (which costs money and bandwidth on every press) once length >= 3.
    // Now we coalesce a burst of typing into one request after the user pauses.
    private val addressQuery = MutableStateFlow("")

    init {
        loadUserProfile()
        refreshPendingReviewCount()

        // Debounced address autocomplete pipeline. The 300ms window is the
        // sweet spot: long enough to skip the noise of mid-word typing, short
        // enough that suggestions still feel snappy.
        addressQuery
            .debounce(ADDRESS_DEBOUNCE_MS)
            .distinctUntilChanged()
            .filter { it.length >= ADDRESS_MIN_QUERY_LEN }
            .onEach { query -> fetchAddressPredictions(query) }
            .launchIn(viewModelScope)
    }

    private val _navigationEvent = Channel<ProfileNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun logout() {
        viewModelScope.launch {
            AppModule.authRepository.logout()
            AppModule.profileLocalStore.clear()
            _navigationEvent.send(ProfileNavigationEvent.NavigateToLogin)
        }
    }

    private fun loadUserResources(ownerId: Int) {
        viewModelScope.launch {
            try {
                userResources = AppModule.resourceRepository.getResourcesByOwner(ownerId)
            } catch (e: Exception) {
                error = "Could not load listings"
            }
        }
    }

    fun loadReviewSummary(userId: Int) {
        viewModelScope.launch {
            // Seller and buyer review fetches are independent — run them in
            // parallel. Previously the second waited on the first, doubling
            // the time-to-paint for the rating stars on the profile.
            val sellerDeferred = async { AppModule.reviewRepository.getUserReviews(userId, "SELLER") }
            val buyerDeferred = async { AppModule.reviewRepository.getUserReviews(userId, "BUYER") }
            val (sellerResult, buyerResult) = awaitAll(sellerDeferred, buyerDeferred)

            sellerResult.onSuccess { reviews ->
                val avg = if (reviews.isEmpty()) 0
                else reviews.map { it.score }.average().roundToInt()
                user = user.copy(sellerRating = avg.coerceIn(0, 5))
            }
            buyerResult.onSuccess { reviews ->
                val avg = if (reviews.isEmpty()) 0
                else reviews.map { it.score }.average().roundToInt()
                user = user.copy(clientRating = avg.coerceIn(0, 5))
            }
        }
    }

    fun refreshPendingReviewCount() {
        viewModelScope.launch {
            AppModule.reviewRepository.getPendingReviews()
                .onSuccess { items -> pendingReviewCount = items.size }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            error = null

            AppModule.userRepository.getMe()
                .onSuccess { dto ->
                    val safeUsername = dto.username.orEmpty()
                    val safeEmail = dto.email.orEmpty()

                    userId = dto.id

                    user = user.copy(
                        id = dto.id,
                        name = dto.name.orEmpty(),
                        email = safeEmail,
                        username = safeUsername,
                        location = dto.address.orEmpty(),
                        university = dto.university?.name.orEmpty(),
                        profilePicture = dto.profilePicture
                    )

                    editDraft = editDraft.copy(
                        name = dto.name.orEmpty(),
                        address = dto.address.orEmpty(),
                        university = dto.university?.name.orEmpty(),
                        universityId = dto.university?.id,
                        course = dto.course?.name.orEmpty(),
                        courseId = dto.course?.id
                    )

                    AppModule.profileLocalStore.saveUser(
                        name = dto.name.orEmpty(),
                        email = safeEmail,
                        username = safeUsername
                    )

                    loadUserResources(dto.id)
                    loadReviewSummary(dto.id)
                }
                .onFailure {
                    error = "Could not load profile"
                }

            isLoading = false
        }
    }

    // ── Edit-profile draft mutators ────────────────────────────

    fun onDraftNameChange(name: String) {
        editDraft = editDraft.copy(name = name)
    }

    fun onDraftUniversitySelected(university: University) {
        editDraft = editDraft.copy(
            university = university.name,
            universityId = university.id,
            course = "",
            courseId = null,
            courses = emptyList()
        )
        viewModelScope.launch {
            AppModule.userRepository.getCoursesByUniversity(university.id)
                .onSuccess { editDraft = editDraft.copy(courses = it) }
        }
    }

    fun onDraftCourseSelected(course: Course) {
        editDraft = editDraft.copy(course = course.name, courseId = course.id)
    }

    fun onDraftAddressChange(query: String) {
        // Mirror the typed text into the draft immediately for the TextField,
        // but push the query into the debounced flow that drives Places calls.
        editDraft = editDraft.copy(address = query)
        if (query.length < ADDRESS_MIN_QUERY_LEN) {
            // Clear stale suggestions as soon as the input drops below the
            // minimum — otherwise the dropdown lingers on what was typed before.
            editDraft = editDraft.copy(addressPredictions = emptyList())
        }
        addressQuery.value = query
    }

    private suspend fun fetchAddressPredictions(query: String) {
        val results = runCatching {
            AppModule.placesRepository
                .getAutocompletePredictions(query, addressSessionToken)
                .map {
                    AddressPrediction(
                        placeId = it.placeId,
                        primaryText = it.getPrimaryText(null).toString(),
                        secondaryText = it.getSecondaryText(null).toString(),
                        fullText = it.getFullText(null).toString()
                    )
                }
        }.getOrDefault(emptyList())

        // Only apply the predictions if the user hasn't moved past this query
        // already (race-safe: a slow response for an old query won't overwrite
        // a newer one). The check is best-effort — we compare against the
        // current draft, not the addressQuery flow, because the draft is what
        // the UI is bound to.
        if (editDraft.address == query) {
            editDraft = editDraft.copy(addressPredictions = results)
        }
    }

    fun onDraftAddressSelected(prediction: AddressPrediction) {
        editDraft = editDraft.copy(
            address = prediction.fullText,
            addressPredictions = emptyList()
        )
        // Stop the in-flight debounce from re-populating predictions with the
        // (now stale) partial query the user was typing before they picked.
        addressQuery.value = prediction.fullText
    }

    fun loadUniversitiesIfNeeded() {
        if (editDraft.universities.isNotEmpty() || editDraft.universitiesLoading) return
        viewModelScope.launch {
            editDraft = editDraft.copy(universitiesLoading = true)
            AppModule.userRepository.getUniversities()
                .onSuccess {
                    editDraft = editDraft.copy(universities = it, universitiesLoading = false)
                }
                .onFailure {
                    editDraft = editDraft.copy(universitiesLoading = false)
                    error = "Could not load universities"
                }

            editDraft.universityId?.let { uniId ->
                AppModule.userRepository.getCoursesByUniversity(uniId)
                    .onSuccess { editDraft = editDraft.copy(courses = it) }
            }
        }
    }

    fun saveEditProfile(onDone: () -> Unit) {
        val draft = editDraft
        viewModelScope.launch {
            isLoading = true
            error = null
            AppModule.userRepository.updateMe(
                name = draft.name.takeIf { it.isNotBlank() },
                address = draft.address.takeIf { it.isNotBlank() },
                courseId = draft.courseId,
                universityId = draft.universityId
            )
                .onSuccess { dto ->
                    val safeUsername = dto.username.orEmpty()
                    user = user.copy(
                        name = dto.name.orEmpty(),
                        email = dto.email.orEmpty(),
                        username = safeUsername,
                        location = dto.address.orEmpty(),
                        university = dto.university?.name.orEmpty(),
                        profilePicture = dto.profilePicture
                    )
                    AppModule.profileLocalStore.saveUser(
                        name = dto.name.orEmpty(),
                        email = dto.email.orEmpty(),
                        username = safeUsername
                    )
                    onDone()
                }
                .onFailure { error = "Could not update profile" }
            isLoading = false
        }
    }

    fun updateUsername(username: String) {
        AppModule.profileLocalStore.updateUsername(username)
        user = user.copy(username = username)
    }

    fun refreshNotificationPermission(granted: Boolean) {
        notificationsEnabled = granted
    }

    fun updateShowLocation(enabled: Boolean) { showLocationInProfile = enabled }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            isLoading = true
            error = null
            AppModule.userRepository.uploadAvatar(uri)
                .onSuccess { url -> user = user.copy(profilePicture = url) }
                .onFailure { error = "Could not update photo" }
            isLoading = false
        }
    }

    private companion object {
        const val ADDRESS_DEBOUNCE_MS = 300L
        const val ADDRESS_MIN_QUERY_LEN = 3
    }
}

data class EditProfileDraft(
    val name: String = "",
    val university: String = "",
    val universityId: Int? = null,
    val course: String = "",
    val courseId: Int? = null,
    val address: String = "",
    val addressPredictions: List<AddressPrediction> = emptyList(),
    val universities: List<University> = emptyList(),
    val universitiesLoading: Boolean = false,
    val courses: List<Course> = emptyList()
)

sealed interface ProfileNavigationEvent {
    data object NavigateToLogin : ProfileNavigationEvent
}