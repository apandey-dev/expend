package com.expend.tracker

import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.FinancialSummary
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.data.models.WalletWithBalance
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WalletTypeAndTemplateTest {

    @Test
    fun allSevenWalletTypes_arePresentWithExactNamesAndDisplayNames() {
        val entries = WalletType.entries
        assertEquals("Must support exactly 7 wallet types", 7, entries.size)

        val expectedDisplayNames = listOf(
            "Cash",
            "Bank Account",
            "Digital Wallet",
            "Credit Card",
            "Savings",
            "Business",
            "Other"
        )

        val actualDisplayNames = entries.map { it.displayName }
        assertEquals(expectedDisplayNames, actualDisplayNames)
    }

    @Test
    fun walletType_fromString_normalizesLegacyAndUnknownSafely() {
        assertEquals(WalletType.CASH, WalletType.fromString("CASH"))
        assertEquals(WalletType.CASH, WalletType.fromString("Cash"))
        assertEquals(WalletType.BANK_ACCOUNT, WalletType.fromString("BANK"))
        assertEquals(WalletType.BANK_ACCOUNT, WalletType.fromString("Bank Account"))
        assertEquals(WalletType.DIGITAL_WALLET, WalletType.fromString("UPI"))
        assertEquals(WalletType.DIGITAL_WALLET, WalletType.fromString("Digital Wallet"))
        assertEquals(WalletType.CREDIT_CARD, WalletType.fromString("CREDIT_CARD"))
        assertEquals(WalletType.CREDIT_CARD, WalletType.fromString("Credit Card"))
        assertEquals(WalletType.SAVINGS, WalletType.fromString("SAVINGS ACCOUNT"))
        assertEquals(WalletType.SAVINGS, WalletType.fromString("Savings"))
        assertEquals(WalletType.BUSINESS, WalletType.fromString("Business"))
        assertEquals(WalletType.OTHER, WalletType.fromString("OTHER"))
        assertEquals(WalletType.OTHER, WalletType.fromString("SomeRandomType"))
        assertEquals(WalletType.OTHER, WalletType.fromString(null))
        assertEquals(WalletType.OTHER, WalletType.fromString(""))
    }

    @Test
    fun creditCardAccounting_tracksLiabilityAndPreventsDoubleCountingOnRepayment() {
        val bankWallet = WalletEntity(id = 1L, name = "HDFC Bank", initialBalancePaise = 2000000L, walletType = WalletType.BANK_ACCOUNT.name) // ₹20,000
        val creditCardWallet = WalletEntity(id = 2L, name = "ICICI Credit Card", initialBalancePaise = 0L, walletType = WalletType.CREDIT_CARD.name) // ₹0 initial

        // 1. User spends ₹5,000 on Groceries with Credit Card
        val expenseTx = TransactionEntity(
            id = 101L,
            title = "Groceries",
            amountPaise = 500000L, // ₹5,000
            type = TransactionType.EXPENSE.name,
            category = "Groceries",
            walletId = creditCardWallet.id,
            dateMillis = System.currentTimeMillis()
        )

        // Calculate Credit Card balance after purchase:
        // Income = 0, Expense = 500000
        val ccBalanceAfterExpense = WalletWithBalance(
            wallet = creditCardWallet,
            incomePaise = 0L,
            expensePaise = expenseTx.amountPaise,
            transactionCount = 1
        )

        // Liability is negative: -₹5,000.00
        assertEquals(-500000L, ccBalanceAfterExpense.currentBalancePaise)

        // 2. User repays Credit Card bill via Transfer from Bank to Credit Card
        val repaymentTransferTx = TransactionEntity(
            id = 102L,
            title = "Credit Card Bill Payment",
            amountPaise = 500000L, // ₹5,000
            type = TransactionType.TRANSFER.name,
            category = "Transfer",
            walletId = bankWallet.id, // Source: Bank
            destinationWalletId = creditCardWallet.id, // Destination: Credit Card
            dateMillis = System.currentTimeMillis()
        )

        // Credit Card after repayment:
        // Income (inbound transfer) = 500000, Expense (purchase) = 500000
        val ccBalanceAfterRepayment = WalletWithBalance(
            wallet = creditCardWallet,
            incomePaise = repaymentTransferTx.amountPaise,
            expensePaise = expenseTx.amountPaise,
            transactionCount = 2
        )

        // Liability is cleared back to 0!
        assertEquals(0L, ccBalanceAfterRepayment.currentBalancePaise)

        // Bank balance after repayment:
        // Initial = 20,000, Expense (outbound transfer) = 5,000 -> Balance = 15,000
        val bankBalanceAfterRepayment = WalletWithBalance(
            wallet = bankWallet,
            incomePaise = 0L,
            expensePaise = repaymentTransferTx.amountPaise,
            transactionCount = 1
        )
        assertEquals(1500000L, bankBalanceAfterRepayment.currentBalancePaise)

        // Combined Net Worth: 15,000 + 0 = ₹15,000
        val totalNetWorth = bankBalanceAfterRepayment.currentBalancePaise + ccBalanceAfterRepayment.currentBalancePaise
        assertEquals(1500000L, totalNetWorth)

        // 3. Verify Monthly Expenses Total does NOT double-count:
        // Only EXPENSE transactions count towards monthly expenses. TRANSFER is excluded.
        val transactions = listOf(expenseTx, repaymentTransferTx)
        var monthlyExpense = 0L
        for (tx in transactions) {
            if (tx.type == TransactionType.EXPENSE.name) {
                monthlyExpense += tx.amountPaise
            }
        }

        // Exactly ₹5,000, NOT ₹10,000!
        assertEquals(500000L, monthlyExpense)
    }

    @Test
    fun templateFiltering_byTransactionType_strictlyMatchesType() {
        val templates = listOf(
            TemplateEntity(id = 1L, name = "Coffee", title = "Espresso", type = TransactionType.EXPENSE.name, category = "Food & Dining"),
            TemplateEntity(id = 2L, name = "Rent", title = "Monthly Rent", type = TransactionType.EXPENSE.name, category = "Bills & Utilities"),
            TemplateEntity(id = 3L, name = "Salary", title = "Monthly Salary", type = TransactionType.INCOME.name, category = "Salary"),
            TemplateEntity(id = 4L, name = "Freelance", title = "Consulting", type = TransactionType.INCOME.name, category = "Freelance")
        )

        val expenseFiltered = templates.filter { it.type == TransactionType.EXPENSE.name }
        assertEquals(2, expenseFiltered.size)
        assertTrue(expenseFiltered.all { it.type == TransactionType.EXPENSE.name })

        val incomeFiltered = templates.filter { it.type == TransactionType.INCOME.name }
        assertEquals(2, incomeFiltered.size)
        assertTrue(incomeFiltered.all { it.type == TransactionType.INCOME.name })

        val transferFiltered = templates.filter { it.type == TransactionType.TRANSFER.name }
        assertTrue("Transfer must have no matching templates", transferFiltered.isEmpty())
    }

    @Test
    fun backupExportAndImport_safelyPreservesWalletTypesWithFallback() {
        val walletsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", 1L)
                put("name", "SBI")
                put("initialBalancePaise", 100000L)
                put("walletType", "BANK_ACCOUNT")
            })
            put(JSONObject().apply {
                put("id", 2L)
                put("name", "Cash Wallet")
                put("initialBalancePaise", 50000L)
                put("walletType", "CASH")
            })
            put(JSONObject().apply {
                put("id", 3L)
                put("name", "Old Wallet Without Type")
                put("initialBalancePaise", 20000L)
                // Missing walletType
            })
            put(JSONObject().apply {
                put("id", 4L)
                put("name", "Legacy UPI")
                put("initialBalancePaise", 30000L)
                put("walletType", "UPI")
            })
        }

        // Parse with fallback logic
        val parsedWallets = mutableListOf<WalletEntity>()
        for (i in 0 until walletsArray.length()) {
            val obj = walletsArray.getJSONObject(i)
            val rawType = obj.optString("walletType", "OTHER")
            val walletType = WalletType.fromString(rawType).name
            parsedWallets.add(
                WalletEntity(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    initialBalancePaise = obj.getLong("initialBalancePaise"),
                    walletType = walletType
                )
            )
        }

        assertEquals(4, parsedWallets.size)
        assertEquals(WalletType.BANK_ACCOUNT.name, parsedWallets[0].walletType)
        assertEquals(WalletType.CASH.name, parsedWallets[1].walletType)
        assertEquals(WalletType.OTHER.name, parsedWallets[2].walletType)
        assertEquals(WalletType.DIGITAL_WALLET.name, parsedWallets[3].walletType)
    }
}
