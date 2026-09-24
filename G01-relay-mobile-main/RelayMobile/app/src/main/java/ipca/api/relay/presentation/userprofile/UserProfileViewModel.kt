package ipca.api.relay.presentation.userprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.ResourceRepository
import ipca.api.relay.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userId: Int,
    private val users: UserRepository,
    private val resources: ResourceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Profile + listings fetch in parallel — they're independent
            // network calls and the screen needs both before rendering its
            // final state. async/await keeps the worst-case latency at
            // max(profileTime, listingsTime) instead of their sum.
            val profileDeferred = async { users.getUser(userId) }
            val resourcesDeferred = async {
                // Owner listings come back as a flat List<Resource> — the
                // repo doesn't expose a Result wrapper here, so we catch
                // exceptions locally and surface them via UiState below.
                runCatching { resources.getResourcesByOwner(userId) }
            }

            val profileResult = profileDeferred.await()
            val resourcesResult = resourcesDeferred.await()

            val profile = profileResult.getOrNull()
            val owned = resourcesResult.getOrNull().orEmpty()

            // Prioritise the profile error in the message — without a
            // profile the screen has nothing to render anyway. If the
            // profile succeeded but listings failed, we still render the
            // header and show a quieter error band over the (empty) list.
            val error = when {
                profile == null -> profileResult.exceptionOrNull()?.message
                    ?: "Could not load this profile"
                resourcesResult.isFailure -> resourcesResult.exceptionOrNull()?.message
                    ?: "Could not load listings"
                else -> null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile,
                    resources = owned,
                    error = error,
                )
            }
        }
    }

    companion object {
        fun factory(userId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserProfileViewModel(
                    userId = userId,
                    users = AppModule.userRepository,
                    resources = AppModule.resourceRepository,
                )
            }
        }
    }
}
