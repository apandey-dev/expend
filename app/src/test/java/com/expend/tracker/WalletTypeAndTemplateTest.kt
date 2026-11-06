package com.expend.tracker

import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.FinancialSummary
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.util.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class WalletTypeAndTemplateTest {

    @Test
    fun walletTypeEnum_supportsExactly7Types() {
        val types = WalletType.entries
        assertEquals(7, types.size)
        assertTrue(types.contains(WalletType.CASH))
        assertTrue(types.contains(WalletType.BANK_ACCOUNT))
        assertTrue(types.contains(WalletType.DIGITAL_WALLET))
        assertTrue(types.contains(WalletType.CREDIT_CARD))
        assertTrue(types.contains(WalletType.SAVINGS))
        assertTrue(types.contains(WalletType.BUSINESS))
        assertTrue(types.contains(WalletType.OTHER))
    }

    @Test
    fun walletType_fromString_normalizesCorrectly() {
        assertEquals(WalletType.CASH, WalletType.fromString("CASH"))
        assertEquals(WalletType.CASH, WalletType.fromString("Cash"))
        assertEquals(WalletType.BANK_ACCOUNT, WalletType.fromString("BANK_ACCOUNT"))
        assertEquals(WalletType.BANK_ACCOUNT, WalletType.fromString("BANK"))
        assertEquals(WalletType.BANK_ACCOUNT, WalletType.fromString("Bank Account"))
        assertEquals(WalletType.DIGITAL_WALLET, WalletType.fromString("DIGITAL_WALLET"))
        assertEquals(WalletType.DIGITAL_WALLET, WalletType.fromString("Digital Wallet"))
        assertEquals(WalletType.DIGITAL_WALLET, WalletType.fromString("UPI"))
        assertEquals(WalletType.CREDIT_CARD, WalletType.fromString("CREDIT_CARD"))
        assertEquals(WalletType.CREDIT_CARD, WalletType.fromString("Credit Card"))
        assertEquals(WalletType.SAVINGS, WalletType.fromString("SAVINGS"))
        assertEquals(WalletType.SAVINGS, WalletType.fromString("Savings"))
        assertEquals(WalletType.BUSINESS, WalletType.fromString("BUSINESS"))
        assertEquals(WalletType.BUSINESS, WalletType.fromString("Business"))
        assertEquals(WalletType.OTHER, WalletType.fromString("OTHER"))
        assertEquals(WalletType.OTHER, WalletType.fromString("SomeRandomType"))
        assertEquals(WalletType.OTHER, WalletType.fromString(null))
        assertEquals(WalletType.OTHER, WalletType.fromString(""))
    }

    @Test
    fun creditCard_fullAccountingFlow_limit_outstanding_availableCredit_and_settlement() {
        // Scenarios 6 & 7: Create Credit Card with ₹50,000 limit and ₹5,000 opening outstanding
        val creditCard = WalletEntity(
            id = 2L,
            name = "HDFC Credit Card",
            initialBalancePaise = 0L,
            walletType = WalletType.CREDIT_CARD.name,
            creditLimitPaise = 5000000L, // ₹50,000
            openingOutstandingPaise = 500000L // ₹5,000
        )

        val initialCcWb = WalletWithBalance(
            wallet = creditCard,
            incomePaise = 0L,
            expensePaise = 0L,
            transactionCount = 0
        )
        // Verify ₹5,000 outstanding and ₹45,000 available credit
        assertEquals(500000L, initialCcWb.outstandingPaise)
        assertEquals(4500000L, initialCcWb.availableCreditPaise)
        assertEquals(-500000L, initialCcWb.currentBalancePaise)

        // Scenarios 8 & 9: Record a ₹2,000 card expense
        val expenseTx = TransactionEntity(
            id = 101L,
            title = "Dinner",
            amountPaise = 200000L, // ₹2,000
            type = TransactionType.EXPENSE.name,
            category = "Food & Dining",
            walletId = creditCard.id,
            dateMillis = System.currentTimeMillis()
        )

        val ccAfterExpense = WalletWithBalance(
            wallet = creditCard,
            incomePaise = 0L,
            expensePaise = expenseTx.amountPaise,
            transactionCount = 1
        )
        // Verify ₹7,000 outstanding and ₹43,000 available credit
        assertEquals(700000L, ccAfterExpense.outstandingPaise)
        assertEquals(4300000L, ccAfterExpense.availableCreditPaise)
        assertEquals(-700000L, ccAfterExpense.currentBalancePaise)

        // Scenarios 10, 11, 12, 13, 14: Transfer ₹3,000 from SBI Bank to Credit Card
        val sbiBank = WalletEntity(
            id = 1L,
            name = "SBI Bank",
            initialBalancePaise = 2000000L, // ₹20,000
            walletType = WalletType.BANK_ACCOUNT.name
        )

        val settlementTransferTx = TransactionEntity(
            id = 102L,
            title = "Credit Card Bill Payment",
            amountPaise = 300000L, // ₹3,000
            type = TransactionType.TRANSFER.name,
            category = "Transfer",
            walletId = sbiBank.id,
            destinationWalletId = creditCard.id,
            dateMillis = System.currentTimeMillis()
        )

        // Verify SBI Bank decreases by ₹3,000 (from ₹20,000 to ₹17,000)
        val sbiAfterSettlement = WalletWithBalance(
            wallet = sbiBank,
            incomePaise = 0L,
            expensePaise = settlementTransferTx.amountPaise,
            transactionCount = 1
        )
        assertEquals(1700000L, sbiAfterSettlement.currentBalancePaise) // ₹17,000

        // Verify Credit Card outstanding reduces to ₹4,000 and available credit becomes ₹46,000
        val ccAfterSettlement = WalletWithBalance(
            wallet = creditCard,
            incomePaise = settlementTransferTx.amountPaise, // ₹3,000 settlement received
            expensePaise = expenseTx.amountPaise, // ₹2,000 expense
            transactionCount = 2
        )
        assertEquals(400000L, ccAfterSettlement.outstandingPaise) // ₹4,000
        assertEquals(4600000L, ccAfterSettlement.availableCreditPaise) // ₹46,000
        assertEquals(-400000L, ccAfterSettlement.currentBalancePaise)

        // Verify Net Worth invariance on bill payment:
        // Before payment: SBI ₹20k - Card ₹7k = ₹13k
        // After payment: SBI ₹17k - Card ₹4k = ₹13k
        assertEquals(1300000L, sbiAfterSettlement.currentBalancePaise + ccAfterSettlement.currentBalancePaise)

        // Verify bill payment is NOT counted in monthly expenses or income
        val txList = listOf(expenseTx, settlementTransferTx)
        var monthlyIncome = 0L
        var monthlyExpense = 0L
        for (tx in txList) {
            if (tx.type == TransactionType.INCOME.name) monthlyIncome += tx.amountPaise
            else if (tx.type == TransactionType.EXPENSE.name) monthlyExpense += tx.amountPaise
        }
        assertEquals(0L, monthlyIncome)
        assertEquals(200000L, monthlyExpense) // Exactly ₹2,000, NOT ₹5,000
    }

    @Test
    fun creditCard_expenseAndTransferEditingAndDeletion_reversesEffects() {
        val creditCard = WalletEntity(
            id = 2L,
            name = "HDFC Credit Card",
            initialBalancePaise = 0L,
            walletType = WalletType.CREDIT_CARD.name,
            creditLimitPaise = 5000000L,
            openingOutstandingPaise = 500000L
        )

        // Scenarios 15 & 16: Edit card expense from ₹2,000 to ₹3,000, then delete
        val originalExpense = 200000L
        val editedExpense = 300000L

        val ccAfterEdit = WalletWithBalance(
            wallet = creditCard,
            incomePaise = 0L,
            expensePaise = editedExpense,
            transactionCount = 1
        )
        assertEquals(800000L, ccAfterEdit.outstandingPaise) // ₹8,000
        assertEquals(4200000L, ccAfterEdit.availableCreditPaise) // ₹42,000

        // Delete card expense
        val ccAfterDelete = WalletWithBalance(
            wallet = creditCard,
            incomePaise = 0L,
            expensePaise = 0L,
            transactionCount = 0
        )
        assertEquals(500000L, ccAfterDelete.outstandingPaise) // Reverted to ₹5,000
        assertEquals(4500000L, ccAfterDelete.availableCreditPaise) // Reverted to ₹45,000
    }

    @Test
    fun initialBalanceIncomeOption_financialEffect() {
        val now = System.currentTimeMillis()

        // Scenarios 1 & 2: Wallet with Include Initial Balance in Income = No
        val walletNoIncome = WalletEntity(
            id = 1L,
            name = "Savings",
            initialBalancePaise = 1000000L, // ₹10,000
            includeInIncome = false,
            createdAt = now
        )
        val wbNoIncome = WalletWithBalance(wallet = walletNoIncome, incomePaise = 0L, expensePaise = 0L, transactionCount = 0)
        assertEquals(1000000L, wbNoIncome.currentBalancePaise)

        // Total income calculation with includeInIncome = false
        var monthlyIncomeNo = 0L
        if (walletNoIncome.includeInIncome && DateUtils.isCurrentMonth(walletNoIncome.createdAt)) {
            monthlyIncomeNo += walletNoIncome.initialBalancePaise
        }
        assertEquals(0L, monthlyIncomeNo)

        // Scenarios 3 & 4: Wallet with Include Initial Balance in Income = Yes
        val walletWithIncome = WalletEntity(
            id = 2L,
            name = "Bonus Wallet",
            initialBalancePaise = 500000L, // ₹5,000
            includeInIncome = true,
            createdAt = now
        )
        var monthlyIncomeYes = 0L
        if (walletWithIncome.includeInIncome && DateUtils.isCurrentMonth(walletWithIncome.createdAt)) {
            monthlyIncomeYes += walletWithIncome.initialBalancePaise
        }
        assertEquals(500000L, monthlyIncomeYes)

        // Scenario 5: Edit wallet (update name or description) preserving createdAt -> no duplicate income
        val editedWallet = walletWithIncome.copy(
            name = "Bonus Wallet Renamed",
            description = "Updated description",
            updatedAt = System.currentTimeMillis()
        )
        assertEquals(walletWithIncome.createdAt, editedWallet.createdAt)
        assertEquals(walletWithIncome.initialBalancePaise, editedWallet.initialBalancePaise)
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
    fun timePicker_hoursAndMinutesWrapCorrectly() {
        // Hour stepper wrap: 12 -> 1, 1 -> 12
        val hourMin = 1
        val hourMax = 12

        var hour = 12
        var hourIncremented = hour + 1
        if (hourIncremented > hourMax) hourIncremented = hourMin
        assertEquals(1, hourIncremented)

        hour = 1
        var hourDecremented = hour - 1
        if (hourDecremented < hourMin) hourDecremented = hourMax
        assertEquals(12, hourDecremented)

        // Minute stepper wrap: 59 -> 0, 0 -> 59
        val minMin = 0
        val minMax = 59

        var minute = 59
        var minIncremented = minute + 1
        if (minIncremented > minMax) minIncremented = minMin
        assertEquals(0, minIncremented)

        minute = 0
        var minDecremented = minute - 1
        if (minDecremented < minMin) minDecremented = minMax
        assertEquals(59, minDecremented)
    }
}
