package ipca.api.relay.presentation.userprofile

import ipca.api.relay.domain.model.PublicUserProfile
import ipca.api.relay.domain.model.Resource

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val profile: PublicUserProfile? = null,
    val resources: List<Resource> = emptyList(),
    val error: String? = null,
)
