package com.expend.tracker.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.R
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.entities.WalletEntity
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.data.models.WalletWithBalance
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSelectionBottomSheet(
    sheetState: SheetState,
    walletsWithBalances: List<WalletWithBalance>,
    selectedWalletId: Long?,
    title: String = "Select Wallet",
    excludeWalletId: Long? = null,
    onSelectWallet: (WalletEntity) -> Unit,
    onCreateWallet: (name: String, initialBalancePaise: Long, type: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var newWalletName by remember { mutableStateOf("") }
    var newWalletBalance by remember { mutableStateOf("") }
    var newWalletType by remember { mutableStateOf(WalletType.CASH) }
    var formError by remember { mutableStateOf<String?>(null) }

    val activeWallets = walletsWithBalances
        .filter { !it.wallet.isArchived && (excludeWalletId == null || it.wallet.id != excludeWalletId) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        showCreateForm = !showCreateForm
                        formError = null
                    }
                ) {
                    Text(
                        text = if (showCreateForm) "Cancel" else "Create Wallet",
                        fontFamily = MaliFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inline Create Wallet Form
            if (showCreateForm) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "New Wallet",
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = newWalletName,
                            onValueChange = {
                                newWalletName = it
                                formError = null
                            },
                            label = { Text("Wallet Name", fontFamily = MaliFamily, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )

                        OutlinedTextField(
                            value = newWalletBalance,
                            onValueChange = {
                                if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                    newWalletBalance = it
                                    formError = null
                                }
                            },
                            label = { Text("Initial Balance (₹)", fontFamily = MaliFamily, fontSize = 12.sp) },
                            placeholder = { Text("0.00", fontFamily = MaliFamily) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )

                        if (formError != null) {
                            Text(
                                text = formError!!,
                                fontFamily = MaliFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        MonochromePrimaryButton(
                            text = "Save Wallet",
                            onClick = {
                                if (newWalletName.trim().isEmpty()) {
                                    formError = "Please enter wallet name"
                                    return@MonochromePrimaryButton
                                }
                                val balancePaise = CurrencyFormatter.parseAmountToPaise(newWalletBalance) ?: 0L
                                onCreateWallet(newWalletName.trim(), balancePaise, newWalletType.name)
                                showCreateForm = false
                                newWalletName = ""
                                newWalletBalance = ""
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Wallets List
            if (activeWallets.isEmpty()) {
                MinimalEmptyState(
                    message = "No available wallets found.",
                    actionText = "Create Wallet",
                    onActionClick = { showCreateForm = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeWallets, key = { it.wallet.id }) { item ->
                        val isSelected = item.wallet.id == selectedWalletId
                        val shape = RoundedCornerShape(10.dp)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = shape
                                )
                                .clickable {
                                    onSelectWallet(item.wallet)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.wallet.name,
                                        fontFamily = MaliFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = WalletType.fromString(item.wallet.walletType).displayName,
                                        fontFamily = MaliFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyFormatter.formatPaise(item.currentBalancePaise),
                                        fontFamily = MaliFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "Selected",
                                            fontFamily = MaliFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    sheetState: SheetState,
    categories: List<CategoryEntity>,
    selectedCategory: String,
    transactionType: TransactionType,
    onSelectCategory: (CategoryEntity) -> Unit,
    onCreateCategory: (name: String, type: TransactionType) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }

    val activeCategories = categories.filter { !it.isArchived }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select ${transactionType.title} Category",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        showCreateForm = !showCreateForm
                        formError = null
                    }
                ) {
                    Text(
                        text = if (showCreateForm) "Cancel" else "Add Category",
                        fontFamily = MaliFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Inline Add Category Form
            if (showCreateForm) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "New ${transactionType.title} Category",
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = {
                                newCategoryName = it
                                formError = null
                            },
                            label = { Text("Category Name", fontFamily = MaliFamily, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            )
                        )

                        if (formError != null) {
                            Text(
                                text = formError!!,
                                fontFamily = MaliFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        MonochromePrimaryButton(
                            text = "Create Category",
                            onClick = {
                                val trimmed = newCategoryName.trim()
                                if (trimmed.isEmpty()) {
                                    formError = "Please enter category name"
                                    return@MonochromePrimaryButton
                                }
                                onCreateCategory(trimmed, transactionType)
                                showCreateForm = false
                                newCategoryName = ""
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category List
            if (activeCategories.isEmpty()) {
                MinimalEmptyState(
                    message = "No categories available for ${transactionType.title.lowercase()}.",
                    actionText = "Add Category",
                    onActionClick = { showCreateForm = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeCategories, key = { it.id }) { cat ->
                        val isSelected = cat.name.equals(selectedCategory, ignoreCase = true)
                        val shape = RoundedCornerShape(10.dp)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = shape
                                )
                                .clickable {
                                    onSelectCategory(cat)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_nav_categories),
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = cat.name,
                                        fontFamily = MaliFamily,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Text(
                                        text = "Selected",
                                        fontFamily = MaliFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
