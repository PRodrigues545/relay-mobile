package ipca.api.relay.domain.model

enum class PaymentMethod(val label: String) {
    BANK_TRANSFER("Bank Transfer"),
    MBWAY("MBWAY"),
    CASH("Cash Payment"),
}
