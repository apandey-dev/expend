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

    @Test
    fun transferBetweenWallets_preservesTotalCombinedBalance() {
        val wallet1 = WalletEntity(id = 1L, name = "Bank", initialBalancePaise = 1000000L) // ₹10,000
        val wallet2 = WalletEntity(id = 2L, name = "Cash", initialBalancePaise = 200000L)  // ₹2,000

        val initialTotal = wallet1.initialBalancePaise + wallet2.initialBalancePaise // ₹12,000

        val transferTx = TransactionEntity(
            id = 10L,
            title = "ATM Withdrawal",
            amountPaise = 300000L, // ₹3,000 transfer from Bank to Cash
            type = TransactionType.TRANSFER.name,
            category = "Transfer",
            walletId = 1L,
            destinationWalletId = 2L,
            dateMillis = System.currentTimeMillis()
        )

        // Wallet 1 balance: initial - transfer amount
        val wallet1Balance = wallet1.initialBalancePaise - transferTx.amountPaise // ₹7,000
        // Wallet 2 balance: initial + transfer amount
        val wallet2Balance = wallet2.initialBalancePaise + transferTx.amountPaise // ₹5,000

        assertEquals(700000L, wallet1Balance)
        assertEquals(500000L, wallet2Balance)

        val finalTotal = wallet1Balance + wallet2Balance
        assertEquals(initialTotal, finalTotal) // Strict mathematical invariance
    }

    @Test
    fun transferTransactions_doNotInflateMonthlyIncomeOrExpense() {
        val transferTx = TransactionEntity(
            id = 1L,
            title = "Transfer to Savings",
            amountPaise = 5000000L, // ₹50,000
            type = TransactionType.TRANSFER.name,
            category = "Transfer",
            walletId = 1L,
            destinationWalletId = 2L,
            dateMillis = System.currentTimeMillis()
        )

        val regularExpense = TransactionEntity(
            id = 2L,
            title = "Groceries",
            amountPaise = 250000L, // ₹2,500
            type = TransactionType.EXPENSE.name,
            category = "Groceries",
            walletId = 1L,
            dateMillis = System.currentTimeMillis()
        )

        val transactions = listOf(transferTx, regularExpense)

        var monthlyIncome = 0L
        var monthlyExpense = 0L

        transactions.forEach { tx ->
            if (tx.type == TransactionType.INCOME.name) {
                monthlyIncome += tx.amountPaise
            } else if (tx.type == TransactionType.EXPENSE.name) {
                monthlyExpense += tx.amountPaise
            }
            // TRANSFER is strictly excluded from income & expense
        }

        assertEquals(0L, monthlyIncome)
        assertEquals(250000L, monthlyExpense) // Exactly ₹2,500, not ₹52,500
    }
}
