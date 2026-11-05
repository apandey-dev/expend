package com.expend.tracker.ui.wallets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletDetailsBottomSheet(
    sheetState: SheetState,
    walletItem: WalletWithBalance,
    onViewTransactions: () -> Unit,
    onEditWallet: () -> Unit,
    onDismiss: () -> Unit
) {
    val wallet = walletItem.wallet
    val typeDisplayName = WalletType.fromString(wallet.walletType).displayName

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = wallet.name,
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = typeDisplayName + if (wallet.isArchived) " • Archived" else "",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyFormatter.formatPaise(walletItem.currentBalancePaise),
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Details rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(
                    label = "Initial Balance",
                    value = CurrencyFormatter.formatPaise(wallet.initialBalancePaise)
                )
                DetailRow(
                    label = "Total Inflows",
                    value = "+ " + CurrencyFormatter.formatPaise(walletItem.incomePaise)
                )
                DetailRow(
                    label = "Total Outflows",
                    value = "- " + CurrencyFormatter.formatPaise(walletItem.expensePaise)
                )
                DetailRow(
                    label = "Transactions Logged",
                    value = "${walletItem.transactionCount}"
                )
                if (wallet.description.isNotBlank()) {
                    DetailRow(label = "Description", value = wallet.description)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Actions: Edit and View Transactions (Equal 50/50 width)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Edit",
                    onClick = {
                        onDismiss()
                        onEditWallet()
                    },
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Transactions",
                    onClick = onViewTransactions,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletActionsBottomSheet(
    sheetState: SheetState,
    walletItem: WalletWithBalance,
    onViewWallet: () -> Unit,
    onViewTransactions: () -> Unit,
    onEditWallet: () -> Unit,
    onArchiveWallet: () -> Unit,
    onRemoveWallet: () -> Unit,
    onDismiss: () -> Unit
) {
    val wallet = walletItem.wallet

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = wallet.name,
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 1. View Wallet
            ActionSheetRow(
                text = "View Wallet Details",
                onClick = onViewWallet
            )

            // 2. View Transactions
            ActionSheetRow(
                text = "View Transactions",
                onClick = onViewTransactions
            )

            // 3. Edit Wallet
            ActionSheetRow(
                text = "Edit Wallet",
                onClick = onEditWallet
            )

            // 4. Archive / Unarchive
            ActionSheetRow(
                text = if (wallet.isArchived) "Unarchive Wallet" else "Archive Wallet",
                onClick = onArchiveWallet
            )

            // 5. Remove Wallet (Safe delete)
            ActionSheetRow(
                text = "Remove Wallet",
                onClick = onRemoveWallet,
                isDestructive = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionSheetRow(
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = MaliFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (isDestructive) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = MaliFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = MaliFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
