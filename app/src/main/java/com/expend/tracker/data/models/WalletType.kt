package com.expend.tracker.data.models

enum class WalletType(val displayName: String) {
    CASH("Cash"),
    BANK_ACCOUNT("Bank Account"),
    DIGITAL_WALLET("Digital Wallet"),
    CREDIT_CARD("Credit Card"),
    SAVINGS("Savings"),
    BUSINESS("Business"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): WalletType {
            if (value.isNullOrBlank()) return OTHER
            val normalized = value.trim()
            return when {
                normalized.equals("CASH", ignoreCase = true) ||
                normalized.equals("Cash", ignoreCase = true) -> CASH

                normalized.equals("BANK_ACCOUNT", ignoreCase = true) ||
                normalized.equals("BANK", ignoreCase = true) ||
                normalized.equals("Bank Account", ignoreCase = true) -> BANK_ACCOUNT

                normalized.equals("DIGITAL_WALLET", ignoreCase = true) ||
                normalized.equals("Digital Wallet", ignoreCase = true) ||
                normalized.equals("UPI", ignoreCase = true) -> DIGITAL_WALLET

                normalized.equals("CREDIT_CARD", ignoreCase = true) ||
                normalized.equals("Credit Card", ignoreCase = true) -> CREDIT_CARD

                normalized.equals("SAVINGS", ignoreCase = true) ||
                normalized.equals("Savings", ignoreCase = true) ||
                normalized.equals("SAVINGS ACCOUNT", ignoreCase = true) ||
                normalized.equals("Savings Account", ignoreCase = true) -> SAVINGS

                normalized.equals("BUSINESS", ignoreCase = true) ||
                normalized.equals("Business", ignoreCase = true) ||
                normalized.equals("Business Account", ignoreCase = true) -> BUSINESS

                normalized.equals("OTHER", ignoreCase = true) ||
                normalized.equals("Other", ignoreCase = true) -> OTHER

                else -> entries.firstOrNull {
                    it.name.equals(normalized, ignoreCase = true) ||
                    it.displayName.equals(normalized, ignoreCase = true)
                } ?: OTHER
            }
        }
    }
}
