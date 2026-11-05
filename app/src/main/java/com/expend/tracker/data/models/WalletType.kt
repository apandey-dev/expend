package com.expend.tracker.data.models

enum class WalletType(val displayName: String) {
    CASH("Cash"),
    BANK("Bank Account"),
    SAVINGS("Savings Account"),
    UPI("UPI"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): WalletType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}
