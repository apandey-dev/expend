package com.expend.tracker.ui.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.expend.tracker.viewmodel.ExpendViewModel

@Composable
fun AllTransactionsScreen(
    viewModel: ExpendViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTransactions by viewModel.allTransactionItems.collectAsState()
    val activeWallets by viewModel.activeWallets.collectAsState()

    TransactionListContent(
        title = "All Transactions",
        subtitle = null,
        transactions = allTransactions,
        activeWallets = activeWallets,
        contextWalletId = null,
        showTransferButton = false,
        showWalletFilterChips = true,
        onNavigateBack = onNavigateBack,
        onNavigateToAddTransaction = onNavigateToAddTransaction,
        onNavigateToTransfer = onNavigateToTransfer,
        onNavigateToEditTransaction = onNavigateToEditTransaction,
        onDeleteTransaction = { viewModel.deleteTransaction(it) },
        modifier = modifier
    )
}
