package com.expend.tracker.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.R
import com.expend.tracker.data.models.TransactionItem
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromeCard
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.TransactionDetailsBottomSheet
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.util.DateUtils
import com.expend.tracker.viewmodel.ExpendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddIncome: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.financialSummary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()

    var selectedTransactionForDetails by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }
    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Double-back-to-exit protection on root Home destination
    var lastBackTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (selectedTransactionForDetails != null) {
            selectedTransactionForDetails = null
            return@BackHandler
        }
        val now = System.currentTimeMillis()
        if (now - lastBackTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackTime = now
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── HEADER ─────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Expend",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = DateUtils.formatDate(System.currentTimeMillis()),
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_action_settings),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ─── FINANCIAL SUMMARY: CHARCOAL BANNER DESIGN ────────────────────────
        item {
            val isDark = isSystemInDarkTheme()
            val bannerBg = if (isDark) Color(0xFF242424) else Color(0xFF1E1E1E)
            val cardBg = MaterialTheme.colorScheme.surface
            val shape = RoundedCornerShape(16.dp)

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
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Charcoal Banner Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bannerBg)
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(end = 64.dp)
                        ) {
                            Text(
                                text = "Total Balance",
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.formatPaise(summary.totalBalancePaise),
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                        }

                        // Subtle wallet watermark icon in the background
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stamp_other),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.22f),
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 4.dp, y = 2.dp)
                        )
                    }

                    // Thin Divider between Banner and Monthly Summary
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )

                    // Bottom Light Section: Monthly Income & Expense (Strictly NO arrows)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Monthly Income
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Monthly Income",
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "+ " + CurrencyFormatter.formatPaise(summary.monthlyIncomePaise),
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .width(0.5.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        )

                        // Monthly Expense
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Monthly Expense",
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "- " + CurrencyFormatter.formatPaise(summary.monthlyExpensePaise),
                                fontFamily = MaliFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ─── SEPARATE TRANSACTION ACTIONS: ADD INCOME / ADD EXPENSE ───────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MonochromePrimaryButton(
                    text = "Add Income",
                    onClick = onNavigateToAddIncome,
                    modifier = Modifier.weight(1f)
                )
                MonochromePrimaryButton(
                    text = "Add Expense",
                    onClick = onNavigateToAddExpense,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ─── RECENT TRANSACTIONS HEADER ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (recentTransactions.isNotEmpty()) {
                    Text(
                        text = "View all",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onNavigateToAllTransactions)
                    )
                }
            }
        }

        // ─── RECENT TRANSACTIONS LIST ────────────────────────────────────────
        if (recentTransactions.isEmpty()) {
            item {
                MinimalEmptyState(
                    message = "No transactions yet",
                    actionText = "Add Expense",
                    onActionClick = onNavigateToAddExpense
                )
            }
        } else {
            items(recentTransactions, key = { it.transaction.id }) { item ->
                RecentTransactionRow(
                    item = item,
                    onClick = { selectedTransactionForDetails = item },
                    onLongClick = { onNavigateToEditTransaction(item.transaction.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ─── TRANSACTION DETAILS BOTTOM SHEET ─────────────────────────────────────
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

    // ─── DELETE TRANSACTION CONFIRMATION ──────────────────────────────────────
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

/**
 * Strict Rule: Displays ONLY:
 * 1. Title
 * 2. Category · Payment Mode
 * 3. Amount
 * (No wallet name, no note, no individual date)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentTransactionRow(
    item: TransactionItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val tx = item.transaction
    val type = TransactionType.fromString(tx.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
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
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Amount with + or - or ⇄ indicator
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
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
