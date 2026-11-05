package com.expend.tracker.ui.wallets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.ui.components.ConfirmationBottomSheet
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WalletsScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddWallet: () -> Unit,
    onNavigateToEditWallet: (Long) -> Unit,
    onNavigateToTransactionsForWallet: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val walletsWithBalances by viewModel.walletsWithBalances.collectAsState()

    var selectedWalletForDetails by remember { mutableStateOf<WalletWithBalance?>(null) }
    var selectedWalletForActions by remember { mutableStateOf<WalletWithBalance?>(null) }
    var walletToArchiveConfirm by remember { mutableStateOf<WalletWithBalance?>(null) }
    var walletToRemoveConfirm by remember { mutableStateOf<WalletWithBalance?>(null) }
    var removeNoticeMessage by remember { mutableStateOf<String?>(null) }

    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ─── HEADER ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wallets",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sources of funds & current balances",
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── ADD WALLET BUTTON (Text-only) ──────────────────────────────────
        MonochromePrimaryButton(
            text = "Add Wallet",
            onClick = onNavigateToAddWallet
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ─── 2-COLUMN WALLET GRID ───────────────────────────────────────────
        if (walletsWithBalances.isEmpty()) {
            MinimalEmptyState(
                message = "No wallets created yet. Add a wallet like Cash, Bank Account, or UPI to start tracking balances.",
                actionText = "Add Wallet",
                onActionClick = onNavigateToAddWallet
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(walletsWithBalances, key = { it.wallet.id }) { item ->
                    WalletGridCard(
                        item = item,
                        onClick = {
                            selectedWalletForDetails = item
                        },
                        onLongClick = {
                            selectedWalletForActions = item
                        }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // ─── WALLET DETAILS BOTTOM SHEET ─────────────────────────────────────────
    if (selectedWalletForDetails != null) {
        WalletDetailsBottomSheet(
            sheetState = detailsSheetState,
            walletItem = selectedWalletForDetails!!,
            onViewTransactions = {
                val walletId = selectedWalletForDetails?.wallet?.id
                selectedWalletForDetails = null
                if (walletId != null) {
                    onNavigateToTransactionsForWallet(walletId)
                }
            },
            onEditWallet = {
                val walletId = selectedWalletForDetails?.wallet?.id
                selectedWalletForDetails = null
                if (walletId != null) {
                    onNavigateToEditWallet(walletId)
                }
            },
            onDismiss = { selectedWalletForDetails = null }
        )
    }

    // ─── WALLET ACTIONS BOTTOM SHEET (Long Press) ────────────────────────────
    if (selectedWalletForActions != null) {
        val targetWalletItem = selectedWalletForActions!!
        WalletActionsBottomSheet(
            sheetState = actionsSheetState,
            walletItem = targetWalletItem,
            onViewWallet = {
                selectedWalletForDetails = targetWalletItem
                selectedWalletForActions = null
            },
            onViewTransactions = {
                val walletId = targetWalletItem.wallet.id
                selectedWalletForActions = null
                onNavigateToTransactionsForWallet(walletId)
            },
            onEditWallet = {
                val walletId = targetWalletItem.wallet.id
                selectedWalletForActions = null
                onNavigateToEditWallet(walletId)
            },
            onArchiveWallet = {
                walletToArchiveConfirm = targetWalletItem
                selectedWalletForActions = null
            },
            onRemoveWallet = {
                val wallet = targetWalletItem.wallet
                coroutineScope.launch {
                    val count = viewModel.getTransactionCountForWallet(wallet.id)
                    if (count > 0) {
                        removeNoticeMessage = "Cannot permanently remove '${wallet.name}' because it contains $count transaction(s). Archive the wallet instead to hide it while safely preserving your historical ledger."
                    } else {
                        walletToRemoveConfirm = targetWalletItem
                    }
                    selectedWalletForActions = null
                }
            },
            onDismiss = { selectedWalletForActions = null }
        )
    }

    // ─── ARCHIVE CONFIRMATION (Bottom Sheet for Non-Destructive State Change) ──
    if (walletToArchiveConfirm != null) {
        val item = walletToArchiveConfirm!!
        val isArchived = item.wallet.isArchived
        ConfirmationBottomSheet(
            title = if (isArchived) "Unarchive Wallet?" else "Archive Wallet?",
            message = if (isArchived)
                "Unarchiving '${item.wallet.name}' will make it selectable again for new transactions."
            else
                "Archiving '${item.wallet.name}' will preserve all past transactions and balances, but hides it from new transaction selectors.",
            confirmButtonText = if (isArchived) "Unarchive" else "Archive",
            onConfirm = {
                if (isArchived) {
                    viewModel.unarchiveWallet(item.wallet.id)
                } else {
                    viewModel.archiveWallet(item.wallet.id)
                }
                walletToArchiveConfirm = null
            },
            onDismiss = { walletToArchiveConfirm = null }
        )
    }

    // ─── REMOVE CONFIRMATION (Empty Wallets Only) ─────────────────────────────
    if (walletToRemoveConfirm != null) {
        val item = walletToRemoveConfirm!!
        ConfirmDialog(
            title = "Remove Wallet?",
            message = "Are you sure you want to permanently remove '${item.wallet.name}'? This wallet has 0 transactions.",
            confirmButtonText = "Remove",
            onConfirm = {
                viewModel.deleteEmptyWallet(
                    wallet = item.wallet,
                    onSuccess = { walletToRemoveConfirm = null },
                    onError = { err ->
                        removeNoticeMessage = err
                        walletToRemoveConfirm = null
                    }
                )
            },
            onDismiss = { walletToRemoveConfirm = null }
        )
    }

    // ─── NOTICE DIALOG ───────────────────────────────────────────────────────
    if (removeNoticeMessage != null) {
        ConfirmDialog(
            title = "Notice",
            message = removeNoticeMessage!!,
            confirmButtonText = "OK",
            onConfirm = { removeNoticeMessage = null },
            onDismiss = { removeNoticeMessage = null }
        )
    }
}

/**
 * Strict Rule: No icons inside wallet cards.
 * Clean, minimal typographic card.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WalletGridCard(
    item: WalletWithBalance,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val wallet = item.wallet
    val walletType = WalletType.fromString(wallet.walletType)
    val typeDisplayName = walletType.displayName
    val isCreditCard = walletType == WalletType.CREDIT_CARD
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Wallet Name & Type
            Column {
                Text(
                    text = wallet.name,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (wallet.isArchived) "$typeDisplayName • Archived" else typeDisplayName,
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: Current Balance / Outstanding
            Column {
                if (isCreditCard && item.currentBalancePaise < 0) {
                    Text(
                        text = "Outstanding",
                        fontFamily = MaliFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencyFormatter.formatPaise(item.currentBalancePaise),
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
