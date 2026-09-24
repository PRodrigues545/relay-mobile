package ipca.api.relay.presentation.create

import android.net.Uri
import ipca.api.relay.domain.model.RemoteCategory

data class CreateResourceUiState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val categoryId: Int? = null,
    val type: String = "SELL",          // SELL, BORROW, BOTH
    val condition: String = "GOOD",
    val borrowDuration: String = "",
    val zones: String = "",
    val imageUri: Uri? = null,
    val existingImageUrl: String? = null,
    val editingResourceId: Int? = null,
    val categories: List<RemoteCategory> = emptyList(),
    val categoriesLoading: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isEditMode: Boolean get() = editingResourceId != null
}
