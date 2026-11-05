package com.expend.tracker.ui.wallets

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.ui.components.ConfirmationBottomSheet
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWalletScreen(
    viewModel: ExpendViewModel,
    walletId: Long? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = walletId != null && walletId > 0L

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.CASH) }
    var initialBalanceInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var originalName by remember { mutableStateOf("") }
    var originalBalance by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val hasUnsavedChanges = if (isEditing) {
        name != originalName || initialBalanceInput != originalBalance
    } else {
        name.isNotBlank() || initialBalanceInput.isNotBlank() || description.isNotBlank()
    }

    BackHandler {
        if (hasUnsavedChanges && !isSubmitting) {
            showDiscardConfirm = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(walletId) {
        if (isEditing) {
            val wallet = viewModel.getWallet(walletId!!)
            if (wallet != null) {
                name = wallet.name
                selectedType = WalletType.fromString(wallet.walletType)
                initialBalanceInput = CurrencyFormatter.paiseToEditableString(wallet.initialBalancePaise)
                description = wallet.description
                originalName = wallet.name
                originalBalance = initialBalanceInput
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── CLEAN HEADER (No Back Icon in Header) ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Wallet" else "Create Wallet",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // ─── WALLET NAME ─────────────────────────────────────────────────────
        MonochromeTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = "Wallet Name",
            placeholder = "e.g., Cash, SBI Savings, HDFC, Paytm"
        )

        // ─── INITIAL BALANCE ─────────────────────────────────────────────────
        MonochromeTextField(
            value = initialBalanceInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    initialBalanceInput = input
                    errorMessage = null
                }
            },
            label = "Initial Balance (₹)",
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = {
                Text(
                    text = "₹",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )

        // ─── WALLET TYPE SELECTOR ────────────────────────────────────────────
        Column {
            Text(
                text = "Wallet Type",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WalletType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    val shape = RoundedCornerShape(10.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = shape
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type.displayName,
                                fontFamily = MaliFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
            }
        }

        // ─── OPTIONAL DESCRIPTION ────────────────────────────────────────────
        MonochromeTextField(
            value = description,
            onValueChange = { description = it },
            label = "Description (Optional)",
            placeholder = "Notes about this wallet...",
            singleLine = false,
            maxLines = 3
        )

        // ─── ERROR MESSAGE ───────────────────────────────────────────────────
        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage!!,
                fontFamily = MaliFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── SAVE BUTTON (Text-only) ─────────────────────────────────────────
        MonochromePrimaryButton(
            text = if (isEditing) "Save Changes" else "Save Wallet",
            enabled = !isSubmitting,
            onClick = {
                val trimmedName = name.trim()
                val parsedPaise = if (initialBalanceInput.isBlank()) 0L else CurrencyFormatter.parseAmountToPaise(initialBalanceInput)

                when {
                    trimmedName.isEmpty() -> {
                        errorMessage = "Please enter a wallet name."
                    }
                    parsedPaise == null -> {
                        errorMessage = "Please enter a valid initial balance."
                    }
                    else -> {
                        isSubmitting = true
                        viewModel.saveWallet(
                            id = if (isEditing) walletId!! else 0L,
                            name = trimmedName,
                            initialBalancePaise = parsedPaise,
                            walletType = selectedType.name,
                            description = description.trim(),
                            onComplete = {
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ─── DISCARD CHANGES CONFIRMATION (Bottom Sheet for Non-Destructive Confirmation) ──
    if (showDiscardConfirm) {
        ConfirmationBottomSheet(
            title = "Discard Changes?",
            message = "You have unsaved changes. Are you sure you want to discard them?",
            confirmButtonText = "Discard",
            onConfirm = {
                showDiscardConfirm = false
                onNavigateBack()
            },
            onDismiss = { showDiscardConfirm = false }
        )
    }
}
