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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.R
import com.expend.tracker.data.models.TransactionItem
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.components.TransactionDetailsBottomSheet
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.util.DateUtils
import com.expend.tracker.viewmodel.ExpendViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val activeWallets by viewModel.activeWallets.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedType by viewModel.selectedFilterType.collectAsState()
    val selectedWalletId by viewModel.selectedWalletFilterId.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTransactionForDetails by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeFilteredWallet = activeWallets.firstOrNull { it.id == selectedWalletId }

    // Native Back Handling
    BackHandler {
        when {
            selectedTransactionForDetails != null -> selectedTransactionForDetails = null
            transactionToDelete != null -> transactionToDelete = null
            isSearchExpanded -> {
                isSearchExpanded = false
                viewModel.setSearchQuery("")
            }
            selectedWalletId != null -> {
                viewModel.setWalletFilter(null)
            }
        }
    }

    // Group transactions by local calendar date (Newest date first, newest transaction in date first)
    val groupedTransactions = remember(transactions) {
        val zoneId = ZoneId.systemDefault()
        transactions
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
        // ─── TOP HEADER WITH SEARCH ICON & TITLE ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Search toggle icon
                IconButton(
                    onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) {
                            viewModel.setSearchQuery("")
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
                    onValueChange = { viewModel.setSearchQuery(it) },
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
                                    .clickable { viewModel.setSearchQuery("") }
                                    .padding(8.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── TRANSFER BETWEEN WALLETS ACTION ─────────────────────────────────
        MonochromeOutlinedButton(
            text = "Transfer Between Wallets",
            onClick = onNavigateToTransfer
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ─── ACTIVE WALLET FILTER BANNER ─────────────────────────────────────
        if (activeFilteredWallet != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallet: ${activeFilteredWallet.name}",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clear Filter",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { viewModel.setWalletFilter(null) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ─── TYPE FILTER CHIPS ───────────────────────────────────────────────
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // All
            item {
                FilterChip(
                    text = "All",
                    isSelected = selectedType == null,
                    onClick = { viewModel.setFilterType(null) }
                )
            }
            // Expense
            item {
                FilterChip(
                    text = "Expenses",
                    isSelected = selectedType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilterType(TransactionType.EXPENSE) }
                )
            }
            // Income
            item {
                FilterChip(
                    text = "Incomes",
                    isSelected = selectedType == TransactionType.INCOME,
                    onClick = { viewModel.setFilterType(TransactionType.INCOME) }
                )
            }
            // Transfer
            item {
                FilterChip(
                    text = "Transfers",
                    isSelected = selectedType == TransactionType.TRANSFER,
                    onClick = { viewModel.setFilterType(TransactionType.TRANSFER) }
                )
            }

            // Wallet filter chips
            items(activeWallets, key = { it.id }) { wallet ->
                FilterChip(
                    text = wallet.name,
                    isSelected = selectedWalletId == wallet.id,
                    onClick = {
                        if (selectedWalletId == wallet.id) {
                            viewModel.setWalletFilter(null)
                        } else {
                            viewModel.setWalletFilter(wallet.id)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── TRANSACTIONS LIST GROUPED BY DATE ───────────────────────────────
        if (transactions.isEmpty()) {
            MinimalEmptyState(
                message = if (searchQuery.isNotEmpty() || selectedType != null || selectedWalletId != null) {
                    "No matching transactions found"
                } else {
                    "No transactions recorded yet"
                },
                actionText = if (searchQuery.isEmpty() && selectedType == null && selectedWalletId == null)
                    "Add Expense"
                else
                    "Clear Filters",
                onActionClick = {
                    if (searchQuery.isNotEmpty() || selectedType != null || selectedWalletId != null) {
                        viewModel.setSearchQuery("")
                        viewModel.setFilterType(null)
                        viewModel.setWalletFilter(null)
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
                            onClick = { selectedTransactionForDetails = item },
                            onLongClick = { onNavigateToEditTransaction(item.transaction.id) }
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
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
                viewModel.deleteTransaction(item.transaction.id)
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

/**
 * Strict Rule: Displays ONLY:
 * 1. Transaction title
 * 2. Category · Payment mode
 * 3. Amount
 * (Never shows wallet name, note, or individual date)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionListItem(
    item: TransactionItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tx = item.transaction
    val type = TransactionType.fromString(tx.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp)
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
                val subText = if (type == TransactionType.TRANSFER) {
                    "Transfer"
                } else if (tx.paymentMode.isNotBlank()) {
                    "${tx.category} · ${tx.paymentMode}"
                } else {
                    tx.category
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
            val prefix = when (type) {
                TransactionType.INCOME -> "+ "
                TransactionType.EXPENSE -> "- "
                TransactionType.TRANSFER -> "⇄ "
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
