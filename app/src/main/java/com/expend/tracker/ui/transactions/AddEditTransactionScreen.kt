package com.expend.tracker.ui.transactions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmationBottomSheet
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.DatePickerBottomSheet
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeSegmentedTypeSelector
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.components.TimePickerBottomSheet
import com.expend.tracker.ui.templates.TemplateSelectionBottomSheet
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: ExpendViewModel,
    transactionId: Long? = null,
    templateId: Long? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletsWithBalances by viewModel.walletsWithBalances.collectAsState()
    val activeWallets by viewModel.activeWallets.collectAsState()
    val activeExpenseCategories by viewModel.activeExpenseCategories.collectAsState()
    val activeIncomeCategories by viewModel.activeIncomeCategories.collectAsState()
    val savedTemplates by viewModel.templates.collectAsState()
    val userPaymentModes by viewModel.userPaymentModes.collectAsState()

    var transactionType by remember { mutableStateOf(initialType) }
    var amountInput by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food & Dining") }
    // Payment mode initially empty per requirement
    var paymentMode by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableLongStateOf(0L) }
    var destinationWalletId by remember { mutableStateOf<Long?>(null) }
    var dateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }

    var originalTitle by remember { mutableStateOf("") }
    var originalAmount by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    // Bottom Sheets State
    var showSourceWalletSheet by remember { mutableStateOf(false) }
    var showDestWalletSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showPaymentModeSheet by remember { mutableStateOf(false) }
    var showDatePickerSheet by remember { mutableStateOf(false) }
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var showTemplateSheet by remember { mutableStateOf(false) }

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val destSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paymentModeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val datePickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val timePickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val templateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditing = transactionId != null && transactionId > 0L
    val hasUnsavedChanges = amountInput.isNotBlank() || title.isNotBlank() || note.isNotBlank()

    // Native Back Handling
    BackHandler {
        when {
            showPaymentModeSheet -> showPaymentModeSheet = false
            showTemplateSheet -> showTemplateSheet = false
            showDatePickerSheet -> showDatePickerSheet = false
            showTimePickerSheet -> showTimePickerSheet = false
            showCategorySheet -> showCategorySheet = false
            showSourceWalletSheet -> showSourceWalletSheet = false
            showDestWalletSheet -> showDestWalletSheet = false
            showDeleteConfirm -> showDeleteConfirm = false
            showDiscardConfirm -> showDiscardConfirm = false
            hasUnsavedChanges && !isSubmitting && !isEditing -> showDiscardConfirm = true
            isEditing && (title != originalTitle || amountInput != originalAmount) && !isSubmitting -> showDiscardConfirm = true
            else -> onNavigateBack()
        }
    }

    // Default wallet initialization
    LaunchedEffect(activeWallets) {
        if (selectedWalletId == 0L && activeWallets.isNotEmpty()) {
            selectedWalletId = activeWallets.first().id
        }
        if (destinationWalletId == null && activeWallets.size > 1) {
            destinationWalletId = activeWallets.firstOrNull { it.id != selectedWalletId }?.id
        }
    }

    // Default category based on type
    LaunchedEffect(transactionType, activeExpenseCategories, activeIncomeCategories) {
        val currentCats = if (transactionType == TransactionType.EXPENSE) activeExpenseCategories else activeIncomeCategories
        if (transactionType != TransactionType.TRANSFER && currentCats.none { it.name.equals(category, ignoreCase = true) }) {
            category = currentCats.firstOrNull()?.name ?: "General"
        }
    }

    // Load existing transaction or template
    LaunchedEffect(transactionId, templateId) {
        if (transactionId != null && transactionId > 0L) {
            val tx = viewModel.getTransaction(transactionId)
            if (tx != null) {
                transactionType = TransactionType.fromString(tx.type)
                amountInput = CurrencyFormatter.paiseToEditableString(tx.amountPaise)
                title = tx.title
                category = tx.category
                paymentMode = tx.paymentMode
                selectedWalletId = tx.walletId
                destinationWalletId = tx.destinationWalletId
                dateMillis = tx.dateMillis
                note = tx.note

                originalTitle = tx.title
                originalAmount = CurrencyFormatter.paiseToEditableString(tx.amountPaise)
            }
        } else if (templateId != null && templateId > 0L) {
            val tpl = viewModel.getTemplate(templateId)
            if (tpl != null) {
                transactionType = TransactionType.fromString(tpl.type)
                title = tpl.title
                category = tpl.category
                if (tpl.defaultWalletId != null && activeWallets.any { it.id == tpl.defaultWalletId }) {
                    selectedWalletId = tpl.defaultWalletId
                }
                if (tpl.defaultAmountPaise != null) {
                    amountInput = CurrencyFormatter.paiseToEditableString(tpl.defaultAmountPaise)
                }
                note = tpl.note
            }
        }
    }

    val zoneId = remember { ZoneId.systemDefault() }
    val currentLocalDateTime = remember(dateMillis) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(dateMillis), zoneId)
    }
    val formattedDate = remember(currentLocalDateTime) {
        currentLocalDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
    val formattedTime = remember(currentLocalDateTime) {
        currentLocalDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ─── CLEAN HEADER (No Back Icon in Header) ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Transaction" else "New Transaction",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (!isEditing) {
                TextButton(onClick = { showTemplateSheet = true }) {
                    Text(
                        text = "Use Template",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text(
                        text = "Delete",
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ─── TYPE SELECTOR: EXPENSE / INCOME / TRANSFER ──────────────────────
        MonochromeSegmentedTypeSelector(
            selectedType = transactionType,
            onTypeSelected = { newType ->
                transactionType = newType
                errorMessage = null
                if (newType == TransactionType.TRANSFER) {
                    if (destinationWalletId == null || destinationWalletId == selectedWalletId) {
                        destinationWalletId = activeWallets.firstOrNull { it.id != selectedWalletId }?.id
                    }
                    if (title.isBlank()) {
                        title = "Wallet Transfer"
                    }
                }
            }
        )

        // ─── AMOUNT INPUT (Stored in Paise) ──────────────────────────────────
        MonochromeTextField(
            value = amountInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    amountInput = input
                    errorMessage = null
                }
            },
            label = "Amount (₹)",
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

        // ─── TITLE / DESCRIPTION ─────────────────────────────────────────────
        MonochromeTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = "Title",
            placeholder = if (transactionType == TransactionType.TRANSFER) "e.g., Transfer to Savings" else "e.g., Coffee, Petrol, Grocery"
        )

        // ─── CATEGORY SELECTOR (Only for Expense & Income) ───────────────────
        if (transactionType != TransactionType.TRANSFER) {
            Column {
                Text(
                    text = "Category",
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
                        .clickable { showCategorySheet = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.ifEmpty { "Select Category" },
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
        }

        // ─── PAYMENT MODE (Dropdown Selection Component, Optional) ───────────
        if (transactionType != TransactionType.TRANSFER) {
            Column {
                Text(
                    text = "Payment Mode",
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
                        .clickable { showPaymentModeSheet = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = paymentMode.ifEmpty { "Select Payment Mode" },
                            fontFamily = MaliFamily,
                            fontSize = 14.sp,
                            fontWeight = if (paymentMode.isNotEmpty()) FontWeight.Medium else FontWeight.Normal,
                            color = if (paymentMode.isNotEmpty()) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "▼",
                            fontFamily = MaliFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ─── WALLET SELECTORS ────────────────────────────────────────────────
        if (transactionType == TransactionType.TRANSFER) {
            // TRANSFER: FROM WALLET & TO WALLET
            val fromWallet = activeWallets.firstOrNull { it.id == selectedWalletId }
            val toWallet = activeWallets.firstOrNull { it.id == destinationWalletId }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // From Wallet
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From Wallet",
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
                            .clickable { showSourceWalletSheet = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = fromWallet?.name ?: "Select",
                            fontFamily = MaliFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                // To Wallet
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "To Wallet",
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
                            .clickable { showDestWalletSheet = true }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = toWallet?.name ?: "Select",
                            fontFamily = MaliFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        } else {
            // STANDARD SINGLE WALLET SELECTOR
            val selectedWallet = activeWallets.firstOrNull { it.id == selectedWalletId }
            Column {
                Text(
                    text = "Wallet",
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
                        .clickable { showSourceWalletSheet = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWallet?.name ?: "Select Wallet",
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
        }

        // ─── SEPARATE DATE AND TIME SELECTION ─────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Date Field
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date",
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
                        .clickable { showDatePickerSheet = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            fontFamily = MaliFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Change",
                            fontFamily = MaliFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time Field
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Time",
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
                        .clickable { showTimePickerSheet = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontFamily = MaliFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Change",
                            fontFamily = MaliFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ─── OPTIONAL NOTE ───────────────────────────────────────────────────
        MonochromeTextField(
            value = note,
            onValueChange = { note = it },
            label = "Note (Optional)",
            placeholder = "Additional details...",
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

        Spacer(modifier = Modifier.height(6.dp))

        // ─── ACTION BUTTONS (Equal-Width 50%/50% when editing, or Save) ──────
        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Delete",
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Save",
                    enabled = !isSubmitting,
                    onClick = {
                        val parsedPaise = CurrencyFormatter.parseAmountToPaise(amountInput)
                        when {
                            parsedPaise == null || parsedPaise <= 0L -> {
                                errorMessage = "Please enter a valid amount greater than zero."
                            }
                            title.trim().isEmpty() -> {
                                errorMessage = "Please enter a title."
                            }
                            selectedWalletId == 0L || !activeWallets.any { it.id == selectedWalletId } -> {
                                errorMessage = "Please select a valid source wallet."
                            }
                            transactionType == TransactionType.TRANSFER && (destinationWalletId == null || destinationWalletId == 0L) -> {
                                errorMessage = "Please select a destination wallet for the transfer."
                            }
                            transactionType == TransactionType.TRANSFER && selectedWalletId == destinationWalletId -> {
                                errorMessage = "Source and destination wallets cannot be the same."
                            }
                            else -> {
                                isSubmitting = true
                                viewModel.saveTransaction(
                                    id = transactionId!!,
                                    title = title,
                                    amountPaise = parsedPaise,
                                    type = transactionType,
                                    category = if (transactionType == TransactionType.TRANSFER) "Transfer" else category,
                                    walletId = selectedWalletId,
                                    destinationWalletId = if (transactionType == TransactionType.TRANSFER) destinationWalletId else null,
                                    paymentMode = if (transactionType == TransactionType.TRANSFER) "Transfer" else paymentMode,
                                    dateMillis = dateMillis,
                                    note = note,
                                    onComplete = { onNavigateBack() }
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MonochromePrimaryButton(
                text = "Save",
                enabled = !isSubmitting,
                onClick = {
                    val parsedPaise = CurrencyFormatter.parseAmountToPaise(amountInput)
                    when {
                        parsedPaise == null || parsedPaise <= 0L -> {
                            errorMessage = "Please enter a valid amount greater than zero."
                        }
                        title.trim().isEmpty() -> {
                            errorMessage = "Please enter a title."
                        }
                        selectedWalletId == 0L || !activeWallets.any { it.id == selectedWalletId } -> {
                            errorMessage = "Please create or select a valid wallet first."
                        }
                        transactionType == TransactionType.TRANSFER && (destinationWalletId == null || destinationWalletId == 0L) -> {
                            errorMessage = "Please select a destination wallet for the transfer."
                        }
                        transactionType == TransactionType.TRANSFER && selectedWalletId == destinationWalletId -> {
                            errorMessage = "Source and destination wallets cannot be the same."
                        }
                        else -> {
                            isSubmitting = true
                            viewModel.saveTransaction(
                                id = 0L,
                                title = title,
                                amountPaise = parsedPaise,
                                type = transactionType,
                                category = if (transactionType == TransactionType.TRANSFER) "Transfer" else category,
                                walletId = selectedWalletId,
                                destinationWalletId = if (transactionType == TransactionType.TRANSFER) destinationWalletId else null,
                                paymentMode = if (transactionType == TransactionType.TRANSFER) "Transfer" else paymentMode,
                                dateMillis = dateMillis,
                                note = note,
                                onComplete = { onNavigateBack() }
                            )
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // ─── USE TEMPLATE BOTTOM SHEET ───────────────────────────────────────────
    if (showTemplateSheet) {
        TemplateSelectionBottomSheet(
            sheetState = templateSheetState,
            templates = savedTemplates,
            onSelectTemplate = { selectedTpl ->
                transactionType = TransactionType.fromString(selectedTpl.type)
                title = selectedTpl.title
                category = selectedTpl.category
                if (selectedTpl.defaultAmountPaise != null) {
                    amountInput = CurrencyFormatter.paiseToEditableString(selectedTpl.defaultAmountPaise)
                }
                if (selectedTpl.defaultWalletId != null && activeWallets.any { it.id == selectedTpl.defaultWalletId }) {
                    selectedWalletId = selectedTpl.defaultWalletId
                }
                note = selectedTpl.note
            },
            onNavigateToCreateTemplate = {
                // Navigate to create template if needed
            },
            onDismiss = { showTemplateSheet = false }
        )
    }

    // ─── DATE PICKER BOTTOM SHEET ────────────────────────────────────────────
    if (showDatePickerSheet) {
        DatePickerBottomSheet(
            sheetState = datePickerSheetState,
            initialDate = currentLocalDateTime.toLocalDate(),
            onDateSelected = { newDate ->
                val currentLocalTime = currentLocalDateTime.toLocalTime()
                dateMillis = LocalDateTime.of(newDate, currentLocalTime).atZone(zoneId).toInstant().toEpochMilli()
            },
            onDismiss = { showDatePickerSheet = false }
        )
    }

    // ─── TIME PICKER BOTTOM SHEET ────────────────────────────────────────────
    if (showTimePickerSheet) {
        TimePickerBottomSheet(
            sheetState = timePickerSheetState,
            initialTime = currentLocalDateTime.toLocalTime(),
            onTimeSelected = { newTime ->
                val currentLocalDate = currentLocalDateTime.toLocalDate()
                dateMillis = LocalDateTime.of(currentLocalDate, newTime).atZone(zoneId).toInstant().toEpochMilli()
            },
            onDismiss = { showTimePickerSheet = false }
        )
    }

    // ─── WALLET SELECTION BOTTOM SHEETS ──────────────────────────────────────
    if (showSourceWalletSheet) {
        WalletSelectionBottomSheet(
            sheetState = sourceSheetState,
            walletsWithBalances = walletsWithBalances,
            selectedWalletId = selectedWalletId,
            title = if (transactionType == TransactionType.TRANSFER) "Select Source Wallet" else "Select Wallet",
            excludeWalletId = if (transactionType == TransactionType.TRANSFER) destinationWalletId else null,
            onSelectWallet = { wallet ->
                selectedWalletId = wallet.id
                if (transactionType == TransactionType.TRANSFER && destinationWalletId == wallet.id) {
                    destinationWalletId = activeWallets.firstOrNull { it.id != wallet.id }?.id
                }
            },
            onCreateWallet = { name, initialBalancePaise, type ->
                viewModel.saveWallet(
                    name = name,
                    initialBalancePaise = initialBalancePaise,
                    walletType = type
                )
            },
            onDismiss = { showSourceWalletSheet = false }
        )
    }

    if (showDestWalletSheet) {
        WalletSelectionBottomSheet(
            sheetState = destSheetState,
            walletsWithBalances = walletsWithBalances,
            selectedWalletId = destinationWalletId,
            title = "Select Destination Wallet",
            excludeWalletId = selectedWalletId,
            onSelectWallet = { wallet ->
                destinationWalletId = wallet.id
            },
            onCreateWallet = { name, initialBalancePaise, type ->
                viewModel.saveWallet(
                    name = name,
                    initialBalancePaise = initialBalancePaise,
                    walletType = type
                )
            },
            onDismiss = { showDestWalletSheet = false }
        )
    }

    // ─── PAYMENT MODE SELECTION BOTTOM SHEET ────────────────────────────────
    if (showPaymentModeSheet) {
        PaymentModeSelectionBottomSheet(
            sheetState = paymentModeSheetState,
            selectedPaymentMode = paymentMode,
            onSelectPaymentMode = { mode ->
                paymentMode = mode
            },
            onDismiss = { showPaymentModeSheet = false }
        )
    }

    // ─── CATEGORY SELECTION BOTTOM SHEET ─────────────────────────────────────
    if (showCategorySheet) {
        val currentCategories = if (transactionType == TransactionType.EXPENSE) {
            activeExpenseCategories
        } else {
            activeIncomeCategories
        }

        CategorySelectionBottomSheet(
            sheetState = categorySheetState,
            categories = currentCategories,
            selectedCategory = category,
            transactionType = transactionType,
            onSelectCategory = { cat ->
                category = cat.name
            },
            onCreateCategory = { catName, type ->
                viewModel.saveCategory(name = catName, type = type.name)
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    // ─── DELETE CONFIRMATION DIALOG (Destructive Always AlertDialog) ─────────
    if (showDeleteConfirm && transactionId != null) {
        ConfirmDialog(
            title = "Delete Transaction?",
            message = "This transaction will be permanently removed and your wallet balance will be adjusted accordingly.",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteTransaction(transactionId) {
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // ─── DISCARD CHANGES CONFIRMATION (Non-Destructive Bottom Sheet) ─────────
    if (showDiscardConfirm) {
        ConfirmationBottomSheet(
            title = "Discard Changes?",
            message = "You have unsaved transaction changes. Are you sure you want to discard them?",
            confirmButtonText = "Discard",
            onConfirm = {
                showDiscardConfirm = false
                onNavigateBack()
            },
            onDismiss = { showDiscardConfirm = false }
        )
    }
}
