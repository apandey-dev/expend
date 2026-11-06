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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.expend.tracker.data.models.WalletType
import com.expend.tracker.ui.components.ConfirmationBottomSheet
import com.expend.tracker.ui.components.MonochromeOutlinedButton
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
    onWalletSaved: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isEditing = walletId != null && walletId > 0L

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(WalletType.CASH) }
    var initialBalanceInput by remember { mutableStateOf("") }
    var includeInIncome by remember { mutableStateOf(false) }
    var creditLimitInput by remember { mutableStateOf("") }
    var openingOutstandingInput by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var originalName by remember { mutableStateOf("") }
    var originalBalance by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf(WalletType.CASH) }
    var originalIncludeInIncome by remember { mutableStateOf(false) }
    var originalCreditLimit by remember { mutableStateOf("") }
    var originalOutstanding by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var showWalletTypeSheet by remember { mutableStateOf(false) }

    val walletTypeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isCreditCard = selectedType == WalletType.CREDIT_CARD

    val hasUnsavedChanges = if (isEditing) {
        name != originalName ||
                initialBalanceInput != originalBalance ||
                selectedType != originalType ||
                includeInIncome != originalIncludeInIncome ||
                creditLimitInput != originalCreditLimit ||
                openingOutstandingInput != originalOutstanding
    } else {
        name.isNotBlank() ||
                initialBalanceInput.isNotBlank() ||
                description.isNotBlank() ||
                selectedType != WalletType.CASH ||
                includeInIncome ||
                creditLimitInput.isNotBlank() ||
                openingOutstandingInput.isNotBlank()
    }

    BackHandler {
        when {
            showWalletTypeSheet -> showWalletTypeSheet = false
            hasUnsavedChanges && !isSubmitting -> showDiscardConfirm = true
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(walletId) {
        if (isEditing) {
            val wallet = viewModel.getWallet(walletId!!)
            if (wallet != null) {
                name = wallet.name
                selectedType = WalletType.fromString(wallet.walletType)
                originalType = selectedType
                initialBalanceInput = if (wallet.initialBalancePaise != 0L) {
                    CurrencyFormatter.paiseToEditableString(wallet.initialBalancePaise)
                } else ""
                includeInIncome = wallet.includeInIncome
                creditLimitInput = if (wallet.creditLimitPaise > 0L) {
                    CurrencyFormatter.paiseToEditableString(wallet.creditLimitPaise)
                } else ""
                openingOutstandingInput = if (wallet.openingOutstandingPaise > 0L) {
                    CurrencyFormatter.paiseToEditableString(wallet.openingOutstandingPaise)
                } else ""
                description = wallet.description

                originalName = wallet.name
                originalBalance = initialBalanceInput
                originalIncludeInIncome = includeInIncome
                originalCreditLimit = creditLimitInput
                originalOutstanding = openingOutstandingInput
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
            placeholder = "e.g., Cash, SBI Savings, HDFC Credit Card, Paytm"
        )

        // ─── WALLET TYPE FIELD (Opens ModalBottomSheet per Specification) ───
        Column {
            Text(
                text = "Wallet Type",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { showWalletTypeSheet = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedType.displayName,
                        fontFamily = MaliFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ─── CONDITIONAL FIELDS BASED ON WALLET TYPE ─────────────────────────
        if (isCreditCard) {
            // CREDIT CARD FIELDS
            MonochromeTextField(
                value = creditLimitInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        creditLimitInput = input
                        errorMessage = null
                    }
                },
                label = "Credit Limit (₹)",
                placeholder = "50000.00",
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

            MonochromeTextField(
                value = openingOutstandingInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        openingOutstandingInput = input
                        errorMessage = null
                    }
                },
                label = "Opening Outstanding Amount (₹) - Optional",
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

            // Available Credit preview helper
            val parsedLimit = CurrencyFormatter.parseAmountToPaise(creditLimitInput) ?: 0L
            val parsedOutstanding = CurrencyFormatter.parseAmountToPaise(openingOutstandingInput) ?: 0L
            val calcAvailCredit = (parsedLimit - parsedOutstanding).coerceAtLeast(0L)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Available Credit = Credit Limit − Opening Outstanding",
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Initial Available Credit: ${CurrencyFormatter.formatPaise(calcAvailCredit)}",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            // NORMAL WALLET INITIAL BALANCE
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

            // INCLUDE INITIAL BALANCE IN INCOME OPTION
            Column {
                Text(
                    text = "Include Initial Balance in Income?",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val shape = RoundedCornerShape(8.dp)

                    // Option: No (Default)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(
                                if (!includeInIncome) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { includeInIncome = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No",
                            fontFamily = MaliFamily,
                            fontWeight = if (!includeInIncome) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (!includeInIncome) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Option: Yes
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(
                                if (includeInIncome) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { includeInIncome = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Yes",
                            fontFamily = MaliFamily,
                            fontWeight = if (includeInIncome) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = if (includeInIncome) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

        // ─── ACTION BUTTONS ──────────────────────────────────────────────────
        val onSaveAction = {
            val trimmedName = name.trim()

            if (trimmedName.isEmpty()) {
                errorMessage = "Please enter a wallet name."
            } else if (isCreditCard) {
                val parsedLimit = CurrencyFormatter.parseAmountToPaise(creditLimitInput)
                val parsedOutstanding = if (openingOutstandingInput.isBlank()) 0L else CurrencyFormatter.parseAmountToPaise(openingOutstandingInput)

                when {
                    parsedLimit == null || parsedLimit <= 0L -> {
                        errorMessage = "Please enter a valid credit limit greater than zero."
                    }
                    parsedOutstanding == null || parsedOutstanding < 0L -> {
                        errorMessage = "Please enter a valid opening outstanding amount."
                    }
                    parsedOutstanding > parsedLimit -> {
                        errorMessage = "Opening outstanding amount cannot exceed credit limit."
                    }
                    else -> {
                        isSubmitting = true
                        viewModel.saveWallet(
                            id = if (isEditing) walletId!! else 0L,
                            name = trimmedName,
                            initialBalancePaise = 0L,
                            walletType = selectedType.name,
                            description = description.trim(),
                            includeInIncome = false,
                            creditLimitPaise = parsedLimit,
                            openingOutstandingPaise = parsedOutstanding,
                            onComplete = { savedId ->
                                onWalletSaved?.invoke(savedId)
                                onNavigateBack()
                            }
                        )
                    }
                }
            } else {
                val parsedPaise = if (initialBalanceInput.isBlank()) 0L else CurrencyFormatter.parseAmountToPaise(initialBalanceInput)

                if (parsedPaise == null) {
                    errorMessage = "Please enter a valid initial balance."
                } else {
                    isSubmitting = true
                    viewModel.saveWallet(
                        id = if (isEditing) walletId!! else 0L,
                        name = trimmedName,
                        initialBalancePaise = parsedPaise,
                        walletType = selectedType.name,
                        description = description.trim(),
                        includeInIncome = includeInIncome,
                        creditLimitPaise = 0L,
                        openingOutstandingPaise = 0L,
                        onComplete = { savedId ->
                            onWalletSaved?.invoke(savedId)
                            onNavigateBack()
                        }
                    )
                }
            }
        }

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Cancel",
                    onClick = {
                        if (hasUnsavedChanges) {
                            showDiscardConfirm = true
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Save",
                    enabled = !isSubmitting,
                    onClick = onSaveAction,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MonochromePrimaryButton(
                text = "Save Wallet",
                enabled = !isSubmitting,
                onClick = onSaveAction
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ─── WALLET TYPE SELECTION BOTTOM SHEET ─────────────────────────────
    if (showWalletTypeSheet) {
        WalletTypeSelectionBottomSheet(
            sheetState = walletTypeSheetState,
            selectedType = selectedType,
            onSelectType = { selectedType = it },
            onDismiss = { showWalletTypeSheet = false }
        )
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
