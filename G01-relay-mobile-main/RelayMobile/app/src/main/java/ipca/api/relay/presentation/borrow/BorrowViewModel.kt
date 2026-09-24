package ipca.api.relay.presentation.borrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.repository.OperationRepository
import ipca.api.relay.domain.repository.ResourceRepository
import ipca.api.relay.presentation.purchase.SelectedMeetingPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class BorrowViewModel(
    private val resourceId: Int,
    private val resources: ResourceRepository,
    private val operations: OperationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BorrowUiState(resourceId = resourceId))
    val uiState = _uiState.asStateFlow()

    init {
        loadResource()
    }

    private fun loadResource() {
        viewModelScope.launch {
            val r = resources.getResource(resourceId)
            if (r == null) {
                _uiState.update { it.copy(isLoading = false, error = "Item not found") }
                return@launch
            }
            // Seed startDate with today so the pickers are not both blank on
            // first render; endDate stays blank to force a deliberate choice.
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = r.title,
                    imageUrl = r.imageUrl,
                    descriptionShort = r.description.takeIf { it.isNotBlank() } ?: "",
                    borrowDuration = r.borrowDuration.orEmpty(),
                    zones = r.zones.orEmpty(),
                    startDate = LocalDate.now().toString(),
                    error = null
                )
            }
        }
    }

    fun onStartDateChange(iso: String) {
        _uiState.update { it.copy(startDate = iso) }
    }

    fun onEndDateChange(iso: String) {
        _uiState.update { it.copy(endDate = iso) }
    }

    fun onMeetingPointSelected(id: Int, name: String) {
        _uiState.update { it.copy(selectedMeetingPoint = SelectedMeetingPoint(id, name)) }
    }

    fun onSubmit() {
        val s = _uiState.value
        if (!s.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            operations.createBorrow(
                resourceId = resourceId,
                startDate = s.startDate,
                endDate = s.endDate
            )
                .onSuccess { dto ->
                    _uiState.update {
                        it.copy(isSubmitting = false, createdBorrowId = dto.id)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "Could not complete the request"
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(resourceId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BorrowViewModel(
                    resourceId = resourceId,
                    resources = AppModule.resourceRepository,
                    operations = AppModule.operationRepository,
                )
            }
        }
    }
}