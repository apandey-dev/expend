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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter

@Composable
fun AddEditWalletDialog(
    walletToEdit: WalletEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, initialBalancePaise: Long, type: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(walletToEdit?.name ?: "") }
    var initialBalanceInput by remember {
        mutableStateOf(
            if (walletToEdit != null && walletToEdit.initialBalancePaise != 0L) {
                CurrencyFormatter.paiseToEditableString(walletToEdit.initialBalancePaise)
            } else ""
        )
    }
    var selectedType by remember {
        mutableStateOf(
            WalletType.fromString(walletToEdit?.walletType ?: "CASH")
        )
    }
    var description by remember { mutableStateOf(walletToEdit?.description ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (walletToEdit != null) "Edit Wallet" else "New Wallet",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonochromeTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = "Wallet Name",
                    placeholder = "e.g., Bank Account, Cash, UPI"
                )

                MonochromeTextField(
                    value = initialBalanceInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            initialBalanceInput = input
                        }
                    },
                    label = "Starting Balance (₹)",
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Wallet Type Selector
                Column {
                    Text(
                        text = "Wallet Type",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(WalletType.entries) { type ->
                            val isSelected = selectedType == type
                            val shape = RoundedCornerShape(8.dp)
                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontFamily = MaliFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                MonochromeTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (Optional)",
                    placeholder = "Notes about this wallet...",
                    singleLine = false,
                    maxLines = 2
                )

                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage!!,
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorMessage = "Please enter a wallet name."
                        return@TextButton
                    }
                    val initialPaise = if (initialBalanceInput.isNotEmpty()) {
                        CurrencyFormatter.parseAmountToPaise(initialBalanceInput) ?: 0L
                    } else {
                        0L
                    }
                    onSave(name.trim(), initialPaise, selectedType.name, description.trim())
                }
            ) {
                Text(
                    text = "Save",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = MaliFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    )
}
