package ipca.api.relay.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ipca.api.relay.di.AppModule
import ipca.api.relay.domain.model.PaymentMethod
import ipca.api.relay.domain.repository.OperationRepository
import ipca.api.relay.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PurchaseViewModel(
    private val resourceId: Int,
    private val resources: ResourceRepository,
    private val operations: OperationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState(resourceId = resourceId))
    val uiState = _uiState.asStateFlow()

    init {
        loadResource()
    }

    private fun loadResource() {
        viewModelScope.launch {
            val resource = resources.getResource(resourceId)
            if (resource == null) {
                _uiState.update { it.copy(isLoading = false, error = "Item not found") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    title = resource.title,
                    imageUrl = resource.imageUrl,
                    conditionLabel = describeCondition(resource.condition),
                    price = resource.price ?: 0.0,
                    error = null
                )
            }
        }
    }

    fun onMeetingPointSelected(id: Int, name: String) {
        _uiState.update { it.copy(selectedMeetingPoint = SelectedMeetingPoint(id, name)) }
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            operations.createPurchase(resourceId, state.selectedPaymentMethod!!.name)
                .onSuccess { dto ->
                    android.util.Log.d("PURCHASE", "clientSecret: ${dto.clientSecret}")
                    android.util.Log.d("PURCHASE", "dto: $dto")
                    when (state.selectedPaymentMethod) {
                        PaymentMethod.BANK_TRANSFER -> {
                            // Abre o PaymentSheet do Stripe
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    createdPurchaseId = dto.id,
                                    clientSecret = dto.clientSecret
                                )
                            }
                        }
                        else -> {
                            // Numerário e MBWAY — avança direto sem Stripe
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    createdPurchaseId = dto.id
                                )
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = e.message ?: "Could not complete the purchase"
                        )
                    }
                }
        }
    }

    fun onPaymentError(message: String?) {
        _uiState.update { it.copy(submitError = message ?: "Payment failed") }
    }

    private fun describeCondition(code: String?): String = when (code) {
        "NEW" -> "New"
        "LIKE_NEW" -> "Like new"
        "GOOD" -> "Good"
        "FAIR" -> "Fair condition"
        "POOR" -> "Poor"
        else -> ""
    }

    companion object {
        fun factory(resourceId: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PurchaseViewModel(
                    resourceId = resourceId,
                    resources = AppModule.resourceRepository,
                    operations = AppModule.operationRepository,
                )
            }
        }
    }
}