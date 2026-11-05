package com.expend.tracker.data.models

enum class PaymentMode(val displayName: String) {
    CASH("Cash"),
    UPI("UPI"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other");

    companion object {
        val defaultModes = listOf("Cash", "UPI", "Card", "Bank Transfer", "Other")

        fun fromString(value: String?): String {
            if (value.isNullOrBlank()) return "Cash"
            return defaultModes.firstOrNull { it.equals(value, ignoreCase = true) } ?: value.trim()
        }
    }
}
