package com.expend.tracker.ui.transactions

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.R
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.TransactionItem
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.components.TransactionDetailsBottomSheet
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListContent(
    title: String,
    subtitle: String? = null,
    transactions: List<TransactionItem>,
    activeWallets: List<WalletEntity>,
    contextWalletId: Long? = null,
    showTransferButton: Boolean = true,
    showWalletFilterChips: Boolean = true,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransfer: (() -> Unit)? = null,
    onNavigateToEditTransaction: (Long) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilterType by rememberSaveable { mutableStateOf<TransactionType?>(null) }
    var selectedWalletFilterId by rememberSaveable { mutableStateOf<Long?>(null) }

    var selectedTransactionForDetails by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isLocalBackHandled = selectedTransactionForDetails != null ||
            transactionToDelete != null ||
            isSearchExpanded ||
            selectedWalletFilterId != null ||
            selectedFilterType != null

    BackHandler(enabled = isLocalBackHandled || onNavigateBack != null) {
        when {
            selectedTransactionForDetails != null -> selectedTransactionForDetails = null
            transactionToDelete != null -> transactionToDelete = null
            isSearchExpanded -> {
                isSearchExpanded = false
                searchQuery = ""
            }
            selectedWalletFilterId != null -> selectedWalletFilterId = null
            selectedFilterType != null -> selectedFilterType = null
            onNavigateBack != null -> onNavigateBack()
        }
    }

    // Filter displayed transactions based on local search, type filter, and wallet filter
    val filteredTransactions = remember(
        transactions,
        searchQuery,
        selectedFilterType,
        selectedWalletFilterId,
        contextWalletId
    ) {
        transactions.filter { item ->
            val tx = item.transaction
            val matchesQuery = searchQuery.isEmpty() ||
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.note.contains(searchQuery, ignoreCase = true) ||
                    item.walletName.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedFilterType == null || tx.type == selectedFilterType?.name

            val matchesWallet = if (contextWalletId != null) {
                // When scoped to contextWalletId, item was already filtered for this wallet
                true
            } else {
                selectedWalletFilterId == null ||
                        tx.walletId == selectedWalletFilterId ||
                        (tx.type == TransactionType.TRANSFER.name && tx.destinationWalletId == selectedWalletFilterId)
            }

            matchesQuery && matchesType && matchesWallet
        }
    }

    // Group transactions by local calendar date (Newest date first, newest transaction in date first)
    val groupedTransactions = remember(filteredTransactions) {
        val zoneId = ZoneId.systemDefault()
        filteredTransactions
            .sortedByDescending { it.transaction.dateMillis }
            .groupBy { item ->
                LocalDate.ofInstant(Instant.ofEpochMilli(item.transaction.dateMillis), zoneId)
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ─── TOP HEADER ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = title,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Optional Transfer action button
                if (showTransferButton && onNavigateToTransfer != null) {
                    Text(
                        text = "Transfer",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(onClick = onNavigateToTransfer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Search toggle icon
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) {
                            searchQuery = ""
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_search),
                        contentDescription = "Search",
                        tint = if (isSearchExpanded || searchQuery.isNotEmpty())
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ─── ANIMATED SEARCH INPUT FIELD ─────────────────────────────────────
        AnimatedVisibility(
            visible = isSearchExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                MonochromeTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = "Search transactions",
                    placeholder = "Title, category, or note...",
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                fontFamily = MaliFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(8.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── TYPE & WALLET FILTER CHIPS ──────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // All
            item {
                FilterChip(
                    text = "All",
                    isSelected = selectedFilterType == null,
                    onClick = { selectedFilterType = null }
                )
            }
            // Expense
            item {
                FilterChip(
                    text = "Expenses",
                    isSelected = selectedFilterType == TransactionType.EXPENSE,
                    onClick = { selectedFilterType = TransactionType.EXPENSE }
                )
            }
            // Income
            item {
                FilterChip(
                    text = "Incomes",
                    isSelected = selectedFilterType == TransactionType.INCOME,
                    onClick = { selectedFilterType = TransactionType.INCOME }
                )
            }
            // Transfer
            item {
                FilterChip(
                    text = "Transfers",
                    isSelected = selectedFilterType == TransactionType.TRANSFER,
                    onClick = { selectedFilterType = TransactionType.TRANSFER }
                )
            }

            // Wallet filter chips (Only shown when not already scoped to a specific wallet)
            if (showWalletFilterChips && contextWalletId == null) {
                items(activeWallets, key = { it.id }) { wallet ->
                    FilterChip(
                        text = wallet.name,
                        isSelected = selectedWalletFilterId == wallet.id,
                        onClick = {
                            selectedWalletFilterId = if (selectedWalletFilterId == wallet.id) null else wallet.id
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── TRANSACTIONS LIST GROUPED BY DATE ───────────────────────────────
        val hasActiveFilter = searchQuery.isNotEmpty() || selectedFilterType != null || (contextWalletId == null && selectedWalletFilterId != null)

        if (filteredTransactions.isEmpty()) {
            MinimalEmptyState(
                message = if (hasActiveFilter) {
                    "No matching transactions found."
                } else if (contextWalletId != null) {
                    "No transactions recorded for this wallet yet."
                } else {
                    "No transactions recorded yet."
                },
                actionText = if (hasActiveFilter) "Clear Filters" else "Add Expense",
                onActionClick = {
                    if (hasActiveFilter) {
                        searchQuery = ""
                        selectedFilterType = null
                        selectedWalletFilterId = null
                    } else {
                        onNavigateToAddTransaction()
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                groupedTransactions.forEach { (localDate, dateItems) ->
                    // Date Heading
                    item(key = "date_$localDate") {
                        DateGroupHeader(date = localDate)
                    }

                    // Transactions for this date
                    items(dateItems, key = { it.transaction.id }) { item ->
                        TransactionListItem(
                            item = item,
                            contextWalletId = contextWalletId,
                            onClick = { selectedTransactionForDetails = item },
                            onLongClick = { onNavigateToEditTransaction(item.transaction.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // ─── VIEW TRANSACTION DETAILS BOTTOM SHEET ───────────────────────────────
    if (selectedTransactionForDetails != null) {
        val currentItem = selectedTransactionForDetails!!
        TransactionDetailsBottomSheet(
            sheetState = detailsSheetState,
            item = currentItem,
            onEdit = {
                val id = currentItem.transaction.id
                selectedTransactionForDetails = null
                onNavigateToEditTransaction(id)
            },
            onDelete = {
                transactionToDelete = currentItem
                selectedTransactionForDetails = null
            },
            onDismiss = { selectedTransactionForDetails = null }
        )
    }

    // ─── DELETE TRANSACTION CONFIRMATION DIALOG ──────────────────────────────
    if (transactionToDelete != null) {
        val item = transactionToDelete!!
        ConfirmDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete '${item.transaction.title}'? Your wallet balance will be adjusted automatically.",
            confirmButtonText = "Delete",
            onConfirm = {
                onDeleteTransaction(item.transaction.id)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@Composable
private fun DateGroupHeader(date: LocalDate) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 6.dp)
    ) {
        Text(
            text = date.format(formatter),
            fontFamily = MaliFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = MaliFamily,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionListItem(
    item: TransactionItem,
    contextWalletId: Long?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tx = item.transaction
    val type = TransactionType.fromString(tx.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            )
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))

                val subText = when {
                    type == TransactionType.TRANSFER && contextWalletId != null -> {
                        if (tx.walletId == contextWalletId) "Transfer Out" else "Transfer In"
                    }
                    type == TransactionType.TRANSFER -> "Transfer"
                    tx.paymentMode.isNotBlank() -> "${tx.category} · ${tx.paymentMode}"
                    else -> tx.category
                }

                Text(
                    text = subText,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val amountFormatted = CurrencyFormatter.formatPaise(tx.amountPaise)
            val prefix = when {
                type == TransactionType.INCOME -> "+ "
                type == TransactionType.EXPENSE -> "- "
                type == TransactionType.TRANSFER && contextWalletId != null -> {
                    if (tx.walletId == contextWalletId) "- " else "+ "
                }
                type == TransactionType.TRANSFER -> "⇄ "
                else -> "- "
            }

            Text(
                text = prefix + amountFormatted,
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
