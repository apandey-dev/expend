package com.expend.tracker.ui.transactions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel

@Composable
fun WalletTransactionsScreen(
    viewModel: ExpendViewModel,
    walletId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactionItems.collectAsState()
    val activeWallets by viewModel.activeWallets.collectAsState()
    val walletsWithBalances by viewModel.walletsWithBalances.collectAsState()

    val walletItem = walletsWithBalances.firstOrNull { it.wallet.id == walletId }
    val wallet = walletItem?.wallet

    // Safe error handling if wallet is deleted or unavailable
    if (wallet == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MinimalEmptyState(
                message = "Wallet not found or no longer available.",
                actionText = "Back to Wallets",
                onActionClick = onNavigateBack
            )
        }
        BackHandler { onNavigateBack() }
        return
    }

    val isCreditCard = WalletType.fromString(wallet.walletType) == WalletType.CREDIT_CARD
    val balanceText = if (isCreditCard && walletItem.currentBalancePaise < 0) {
        "Outstanding: " + CurrencyFormatter.formatPaise(-walletItem.currentBalancePaise)
    } else {
        "Balance: " + CurrencyFormatter.formatPaise(walletItem.currentBalancePaise)
    }

    // Wallet-specific transactions:
    // 1. Regular expense and income transactions for this wallet (tx.walletId == walletId)
    // 2. Transfers where this wallet is source (tx.walletId == walletId) OR destination (tx.destinationWalletId == walletId)
    // 3. Each transfer appears exactly once without duplicates
    // 4. Completely excludes transactions from other wallets
    val walletTransactions = remember(allTransactions, walletId) {
        allTransactions.filter { item ->
            val tx = item.transaction
            tx.walletId == walletId || (tx.type == TransactionType.TRANSFER.name && tx.destinationWalletId == walletId)
        }
    }

    TransactionListContent(
        title = "${wallet.name} Transactions",
        subtitle = balanceText,
        transactions = walletTransactions,
        activeWallets = activeWallets,
        contextWalletId = walletId,
        showTransferButton = false,
        showWalletFilterChips = false,
        onNavigateBack = onNavigateBack,
        onNavigateToAddTransaction = onNavigateToAddTransaction,
        onNavigateToTransfer = onNavigateToTransfer,
        onNavigateToEditTransaction = onNavigateToEditTransaction,
        onDeleteTransaction = { viewModel.deleteTransaction(it) },
        modifier = modifier
    )
}
