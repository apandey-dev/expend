package com.expend.tracker.data.models

object CategoryDefaults {
    val expenseCategories = listOf(
        "Food & Dining",
        "Groceries",
        "Transportation",
        "Shopping",
        "Bills & Utilities",
        "Entertainment",
        "Health & Care",
        "Education",
        "General Expense"
    )

    val incomeCategories = listOf(
        "Salary",
        "Freelance",
        "Investments",
        "Gifts",
        "Refund",
        "Other Income"
    )

    fun forType(type: TransactionType): List<String> {
        return if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
    }
}
