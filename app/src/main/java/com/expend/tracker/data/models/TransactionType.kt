package com.expend.tracker.data.models

enum class TransactionType(val title: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: EXPENSE
        }
    }
}
