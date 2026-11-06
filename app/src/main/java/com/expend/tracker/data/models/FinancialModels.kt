package com.expend.tracker.data.models

import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity

data class WalletWithBalance(
    val wallet: WalletEntity,
    val incomePaise: Long,
    val expensePaise: Long,
    val currentBalancePaise: Long = if (wallet.isCreditCard) {
        -((wallet.openingOutstandingPaise + expensePaise - incomePaise).coerceAtLeast(0L))
    } else {
        wallet.initialBalancePaise + incomePaise - expensePaise
    },
    val transactionCount: Int
) {
    val isCreditCard: Boolean get() = wallet.isCreditCard

    val outstandingPaise: Long
        get() = if (isCreditCard) {
            (wallet.openingOutstandingPaise + expensePaise - incomePaise).coerceAtLeast(0L)
        } else 0L

    val availableCreditPaise: Long
        get() = if (isCreditCard) {
            (wallet.creditLimitPaise - outstandingPaise).coerceIn(0L, wallet.creditLimitPaise)
        } else 0L
}

data class FinancialSummary(
    val totalBalancePaise: Long = 0L,
    val monthlyIncomePaise: Long = 0L,
    val monthlyExpensePaise: Long = 0L,
    val activeWalletsCount: Int = 0
)

data class TransactionItem(
    val transaction: TransactionEntity,
    val walletName: String
)
