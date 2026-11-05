package com.expend.tracker.data.models

import com.expend.tracker.R

enum class WalletType(val displayName: String, val stampDrawableRes: Int) {
    CASH("Cash", R.drawable.ic_stamp_cash),
    BANK_ACCOUNT("Bank Account", R.drawable.ic_stamp_bank),
    DIGITAL_WALLET("Digital Wallet", R.drawable.ic_stamp_digital),
    CREDIT_CARD("Credit Card", R.drawable.ic_stamp_card),
    SAVINGS("Savings", R.drawable.ic_stamp_savings),
    BUSINESS("Business", R.drawable.ic_stamp_business),
    OTHER("Other", R.drawable.ic_stamp_other);

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
