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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.R
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
                items(
                    items = walletsWithBalances,
                    key = { it.wallet.id },
                    span = { item ->
                        if (item.isCreditCard) GridItemSpan(2) else GridItemSpan(1)
                    }
                ) { item ->
                    if (item.isCreditCard) {
                        CreditCardWalletCard(
                            item = item,
                            onClick = { selectedWalletForDetails = item },
                            onLongClick = { selectedWalletForActions = item }
                        )
                    } else {
                        NormalWalletCard(
                            item = item,
                            onClick = { selectedWalletForDetails = item },
                            onLongClick = { selectedWalletForActions = item }
                        )
                    }
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
private fun CreditCardWalletCard(
    item: WalletWithBalance,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val wallet = item.wallet
    val shape = RoundedCornerShape(16.dp)
    val isDark = isSystemInDarkTheme()
    val bannerBg = if (isDark) Color(0xFF242424) else Color(0xFF1E1E1E)
    val cardBg = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBg)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top charcoal banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 64.dp)
                ) {
                    Text(
                        text = wallet.name,
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (wallet.isArchived) "Credit Card • Archived" else "Credit Card",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Subtle oversized credit card watermark icon on right
                Icon(
                    painter = painterResource(id = R.drawable.ic_stamp_card),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.28f),
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 4.dp, y = 2.dp)
                )
            }

            // Bottom light section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Outstanding",
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.formatPaise(item.outstandingPaise),
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Limit",
                            fontFamily = MaliFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaise(wallet.creditLimitPaise),
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .width(0.5.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Available",
                            fontFamily = MaliFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaise(item.availableCreditPaise),
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalWalletCard(
    item: WalletWithBalance,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val wallet = item.wallet
    val walletType = WalletType.fromString(wallet.walletType)
    val typeDisplayName = walletType.displayName
    val shape = RoundedCornerShape(14.dp)
    val isDark = isSystemInDarkTheme()
    val bannerBg = if (isDark) Color(0xFF242424) else Color(0xFF1E1E1E)
    val cardBg = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBg)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top charcoal banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bannerBg)
                    .padding(horizontal = 10.dp, vertical = 9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = walletType.stampDrawableRes),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(1.dp)
                            .height(22.dp)
                            .background(Color.White.copy(alpha = 0.45f))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wallet.name,
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (wallet.isArchived) "$typeDisplayName • Archived" else typeDisplayName,
                            fontFamily = MaliFamily,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Bottom light section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Balance",
                    fontFamily = MaliFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatPaise(item.currentBalancePaise),
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
