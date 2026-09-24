package ipca.api.relay.presentation.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipca.api.relay.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateResourceViewModel(
    private val repository: ResourceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateResourceUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(price = v) }
    fun onCategoryChange(v: Int) = _uiState.update { it.copy(categoryId = v) }
    fun onTypeChange(v: String) = _uiState.update { it.copy(type = v) }
    fun onConditionChange(v: String) = _uiState.update { it.copy(condition = v) }
    fun onBorrowDurationChange(v: String) = _uiState.update { it.copy(borrowDuration = v) }
    fun onZonesChange(v: String) = _uiState.update { it.copy(zones = v) }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri) }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(categoriesLoading = true) }
            repository.getCategories()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            categories = list,
                            categoriesLoading = false,
                            // Don't auto-select in edit mode — loadForEdit will
                            // set categoryId from the existing resource.
                            categoryId = it.categoryId ?: if (!it.isEditMode) list.firstOrNull()?.id else null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            categoriesLoading = false,
                            error = "Could not load categories"
                        )
                    }
                }
        }
    }

    /**
     * Switch the screen into edit mode for an existing resource. Fields are
     * seeded from the server so the user sees current values, not blanks.
     */
    fun loadForEdit(resourceId: Int) {
        if (_uiState.value.editingResourceId == resourceId) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editingResourceId = resourceId) }
            val resource = repository.getResource(resourceId)
            if (resource == null) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Listing not found")
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = resource.title,
                    description = resource.description,
                    price = resource.price?.toString().orEmpty(),
                    categoryId = resource.categoryId.takeIf { id -> id != 0 } ?: it.categoryId,
                    type = resource.type,
                    condition = resource.condition ?: "GOOD",
                    borrowDuration = resource.borrowDuration.orEmpty(),
                    zones = resource.zones.orEmpty(),
                    existingImageUrl = resource.imageUrl,
                    error = null
                )
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val categoryId = state.categoryId
        if (categoryId == null) {
            _uiState.update { it.copy(error = "Please select a category") }
            return
        }

        val title = state.title.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(error = "Title cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val price = if (state.type != "BORROW") state.price.toDoubleOrNull() else null
                val borrowDuration = if (state.type != "SELL") state.borrowDuration.ifBlank { null } else null
                val zones = if (state.type != "SELL") state.zones.ifBlank { null } else null

                val editingId = state.editingResourceId
                if (editingId == null) {
                    repository.addResource(
                        title = title,
                        description = state.description,
                        type = state.type,
                        condition = state.condition,
                        price = price,
                        categoryId = categoryId,
                        borrowDuration = borrowDuration,
                        zones = zones,
                        imageUri = state.imageUri
                    )
                } else {
                    repository.updateResource(
                        id = editingId,
                        title = title,
                        description = state.description,
                        type = state.type,
                        condition = state.condition,
                        price = price,
                        categoryId = categoryId,
                        borrowDuration = borrowDuration,
                        zones = zones,
                        imageUri = state.imageUri
                    )
                }
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}