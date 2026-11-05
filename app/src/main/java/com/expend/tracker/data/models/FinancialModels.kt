package com.expend.tracker.data.models

import com.expend.tracker.data.entities.TransactionEntity
import com.expend.tracker.data.entities.WalletEntity

data class WalletWithBalance(
    val wallet: WalletEntity,
    val incomePaise: Long,
    val expensePaise: Long,
    val currentBalancePaise: Long = wallet.initialBalancePaise + incomePaise - expensePaise,
    val transactionCount: Int
)

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
