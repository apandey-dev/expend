package com.expend.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.util.DateUtils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsBottomSheet(
    sheetState: SheetState,
    item: TransactionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val tx = item.transaction
    val type = TransactionType.fromString(tx.type)
    val isIncome = type == TransactionType.INCOME
    val isTransfer = type == TransactionType.TRANSFER

    val amountPrefix = when (type) {
        TransactionType.INCOME -> "+ "
        TransactionType.EXPENSE -> "- "
        TransactionType.TRANSFER -> "⇄ "
    }

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
            // Header: Title and Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.title,
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = type.title,
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = amountPrefix + CurrencyFormatter.formatPaise(tx.amountPaise),
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
            val ldt = try {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(tx.dateMillis), ZoneId.systemDefault())
            } catch (e: Exception) {
                null
            }
            val dateFormatted = ldt?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: DateUtils.formatDate(tx.dateMillis)
            val timeFormatted = ldt?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: ""

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isTransfer) {
                    DetailRow(label = "Category", value = tx.category)
                    if (tx.paymentMode.isNotBlank()) {
                        DetailRow(label = "Payment Mode", value = tx.paymentMode)
                    }
                }
                DetailRow(
                    label = if (isTransfer) "Wallets" else "Wallet",
                    value = item.walletName
                )
                DetailRow(
                    label = "Date",
                    value = dateFormatted
                )
                if (timeFormatted.isNotBlank()) {
                    DetailRow(
                        label = "Time",
                        value = timeFormatted
                    )
                }
                if (tx.note.isNotBlank()) {
                    DetailRow(label = "Note", value = tx.note)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Equal 50/50 Width, Short Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Delete",
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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
