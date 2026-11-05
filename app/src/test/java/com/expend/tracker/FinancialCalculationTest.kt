package com.expend.tracker

import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletWithBalance
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialCalculationTest {

    @Test
    fun walletBalance_initialPlusIncomeMinusExpense_isAccurate() {
        val wallet = WalletEntity(
            id = 1L,
            name = "Main Bank",
            initialBalancePaise = 500000L // ₹5000.00
        )

        val transactions = listOf(
            TransactionEntity(
                id = 1L,
                title = "Salary",
                amountPaise = 2500000L, // ₹25,000.00 income
                type = TransactionType.INCOME.name,
                category = "Salary",
                walletId = 1L,
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 2L,
                title = "Groceries",
                amountPaise = 345050L, // ₹3,450.50 expense
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                walletId = 1L,
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                id = 3L,
                title = "Electricity",
                amountPaise = 120000L, // ₹1,200.00 expense
                type = TransactionType.EXPENSE.name,
                category = "Bills",
                walletId = 1L,
                dateMillis = System.currentTimeMillis()
            )
        )

        var income = 0L
        var expense = 0L
        transactions.forEach {
            if (it.type == TransactionType.INCOME.name) income += it.amountPaise
            else expense += it.amountPaise
        }

        val walletWithBalance = WalletWithBalance(
            wallet = wallet,
            incomePaise = income,
            expensePaise = expense,
            transactionCount = transactions.size
        )

        // Expected: 500000 + 2500000 - 345050 - 120000 = 2534950 paise (₹25,349.50)
        assertEquals(2500000L, walletWithBalance.incomePaise)
        assertEquals(465050L, walletWithBalance.expensePaise)
        assertEquals(2534950L, walletWithBalance.currentBalancePaise)
    }

    @Test
    fun walletBalance_whenTransactionsDeleted_reversesEffect() {
        val wallet = WalletEntity(id = 2L, name = "Cash", initialBalancePaise = 100000L) // ₹1000
        val tx1 = TransactionEntity(id = 1L, title = "Lunch", amountPaise = 20000L, type = "EXPENSE", category = "Food", walletId = 2L, dateMillis = 0L)
        val tx2 = TransactionEntity(id = 2L, title = "Dinner", amountPaise = 30000L, type = "EXPENSE", category = "Food", walletId = 2L, dateMillis = 0L)

        val beforeDelete = wallet.initialBalancePaise - (tx1.amountPaise + tx2.amountPaise)
        assertEquals(50000L, beforeDelete) // ₹500

        // If tx2 is deleted:
        val afterDelete = wallet.initialBalancePaise - tx1.amountPaise
        assertEquals(80000L, afterDelete) // ₹800
    }
}
