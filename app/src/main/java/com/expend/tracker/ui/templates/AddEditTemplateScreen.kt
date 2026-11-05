package com.expend.tracker.ui.templates

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeSegmentedControl
import com.expend.tracker.ui.components.MonochromeSegmentedTypeSelector
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.ui.transactions.CategorySelectionBottomSheet
import com.expend.tracker.ui.transactions.WalletSelectionBottomSheet
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTemplateScreen(
    viewModel: ExpendViewModel,
    templateId: Long? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit,
    onNavigateToCreateWallet: () -> Unit = {},
    onNavigateToCreateCategory: (TransactionType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeWallets by viewModel.activeWallets.collectAsState()
    val walletsWithBalances by viewModel.walletsWithBalances.collectAsState()
    val activeExpenseCategories by viewModel.activeExpenseCategories.collectAsState()
    val activeIncomeCategories by viewModel.activeIncomeCategories.collectAsState()
    val createdWalletId by viewModel.newlyCreatedWalletId.collectAsState()
    val createdCategoryName by viewModel.newlyCreatedCategoryName.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var templateType by rememberSaveable { mutableStateOf(initialType) }
    var defaultAmountInput by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var defaultWalletId by rememberSaveable { mutableStateOf<Long?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var pendingWalletTarget by rememberSaveable { mutableStateOf(false) }
    var isInitialized by rememberSaveable { mutableStateOf(false) }

    // Bottom Sheets
    var showWalletSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    val walletSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isEditing = templateId != null && templateId > 0L
    val hasUnsavedChanges = name.isNotBlank() || title.isNotBlank()

    // Auto-select newly created wallet from full-screen Create Wallet flow
    LaunchedEffect(createdWalletId) {
        createdWalletId?.let { newId ->
            if (pendingWalletTarget) {
                defaultWalletId = newId
                pendingWalletTarget = false
            }
            viewModel.clearNewlyCreatedWalletId()
        }
    }

    // Auto-select newly created category from full-screen Create Category flow
    LaunchedEffect(createdCategoryName) {
        createdCategoryName?.let { newName ->
            category = newName
            viewModel.clearNewlyCreatedCategoryName()
        }
    }

    BackHandler {
        when {
            showCategorySheet -> showCategorySheet = false
            showWalletSheet -> showWalletSheet = false
            showDeleteConfirm -> showDeleteConfirm = false
            showDiscardConfirm -> showDiscardConfirm = false
            hasUnsavedChanges && !isSubmitting && !isEditing -> showDiscardConfirm = true
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(templateId) {
        if (!isInitialized) {
            isInitialized = true
            if (isEditing) {
                val tpl = viewModel.getTemplate(templateId!!)
                if (tpl != null) {
                    name = tpl.name
                    templateType = TransactionType.fromString(tpl.type)
                    if (tpl.defaultAmountPaise != null) {
                        defaultAmountInput = CurrencyFormatter.paiseToEditableString(tpl.defaultAmountPaise)
                    }
                    title = tpl.title
                    category = tpl.category
                    defaultWalletId = tpl.defaultWalletId
                    note = tpl.note
                }
            } else {
                val cats = if (initialType == TransactionType.EXPENSE) activeExpenseCategories else activeIncomeCategories
                if (category.isEmpty() && cats.isNotEmpty()) {
                    category = cats.first().name
                }
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
                text = if (isEditing) "Edit Template" else "New Template",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isEditing) {
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

        // ─── TEMPLATE TYPE SELECTOR (Expense / Income) ──────────────────────
        MonochromeSegmentedControl(
            options = listOf("Expense", "Income"),
            selectedIndex = if (templateType == TransactionType.EXPENSE) 0 else 1,
            onOptionSelected = { index ->
                val newType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                if (newType != templateType) {
                    templateType = newType
                    // Clear incompatible category
                    val validCategories = if (newType == TransactionType.EXPENSE) activeExpenseCategories else activeIncomeCategories
                    if (!validCategories.any { it.name.equals(category, ignoreCase = true) }) {
                        category = ""
                    }
                    errorMessage = null
                }
            }
        )

        // ─── TEMPLATE NAME (How it appears in template list) ─────────────────
        MonochromeTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = "Template Name",
            placeholder = "e.g., Morning Coffee, Monthly Rent"
        )

        // ─── TRANSACTION TITLE ───────────────────────────────────────────────
        MonochromeTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = "Transaction Title",
            placeholder = "e.g., Coffee, Flat 402 Rent"
        )

        // ─── OPTIONAL DEFAULT AMOUNT ─────────────────────────────────────────
        MonochromeTextField(
            value = defaultAmountInput,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' }) {
                    defaultAmountInput = input
                    errorMessage = null
                }
            },
            label = "Default Amount (Optional)",
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

        // ─── CATEGORY FIELD (Opens ModalBottomSheet per Requirement 3) ───────
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
                        color = if (category.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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

        // ─── WALLET FIELD (Opens ModalBottomSheet per Requirement 3) ─────────
        val selectedWallet = activeWallets.firstOrNull { it.id == defaultWalletId }
        Column {
            Text(
                text = "Default Wallet (Optional)",
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
                    .clickable { showWalletSheet = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedWallet?.name ?: "None (Select upon logging)",
                        fontFamily = MaliFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedWallet != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
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

        // ─── OPTIONAL NOTE ───────────────────────────────────────────────────
        MonochromeTextField(
            value = note,
            onValueChange = { note = it },
            label = "Default Note (Optional)",
            placeholder = "Recurring transaction note...",
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

        Spacer(modifier = Modifier.height(8.dp))

        // ─── ACTION BUTTONS (Equal-Width 50%/50% Cancel and Save when editing) ─
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
                    onClick = {
                        val parsedPaise = if (defaultAmountInput.isNotEmpty()) {
                            CurrencyFormatter.parseAmountToPaise(defaultAmountInput)
                        } else null

                        when {
                            name.trim().isEmpty() -> {
                                errorMessage = "Please enter a template name."
                            }
                            title.trim().isEmpty() -> {
                                errorMessage = "Please enter a title for the transaction."
                            }
                            category.trim().isEmpty() -> {
                                errorMessage = "Please select a category."
                            }
                            defaultAmountInput.isNotEmpty() && parsedPaise == null -> {
                                errorMessage = "Please enter a valid amount or leave it empty."
                            }
                            else -> {
                                isSubmitting = true
                                viewModel.saveTemplate(
                                    id = templateId!!,
                                    name = name,
                                    type = templateType,
                                    defaultAmountPaise = parsedPaise,
                                    title = title,
                                    category = category,
                                    defaultWalletId = defaultWalletId,
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
                    val parsedPaise = if (defaultAmountInput.isNotEmpty()) {
                        CurrencyFormatter.parseAmountToPaise(defaultAmountInput)
                    } else null

                    when {
                        name.trim().isEmpty() -> {
                            errorMessage = "Please enter a template name."
                        }
                        title.trim().isEmpty() -> {
                            errorMessage = "Please enter a title for the transaction."
                        }
                        category.trim().isEmpty() -> {
                            errorMessage = "Please select a category."
                        }
                        defaultAmountInput.isNotEmpty() && parsedPaise == null -> {
                            errorMessage = "Please enter a valid amount or leave it empty."
                        }
                        else -> {
                            isSubmitting = true
                            viewModel.saveTemplate(
                                id = 0L,
                                name = name,
                                type = templateType,
                                defaultAmountPaise = parsedPaise,
                                title = title,
                                category = category,
                                defaultWalletId = defaultWalletId,
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

    // ─── WALLET SELECTION BOTTOM SHEET ───────────────────────────────────────
    if (showWalletSheet) {
        WalletSelectionBottomSheet(
            sheetState = walletSheetState,
            walletsWithBalances = walletsWithBalances,
            selectedWalletId = defaultWalletId,
            title = "Select Default Wallet",
            onSelectWallet = { wallet ->
                defaultWalletId = wallet.id
            },
            onNavigateToCreateWallet = {
                showWalletSheet = false
                pendingWalletTarget = true
                onNavigateToCreateWallet()
            },
            onDismiss = { showWalletSheet = false }
        )
    }

    // ─── CATEGORY SELECTION BOTTOM SHEET ─────────────────────────────────────
    if (showCategorySheet) {
        val currentCategories = if (templateType == TransactionType.EXPENSE) {
            activeExpenseCategories
        } else {
            activeIncomeCategories
        }

        CategorySelectionBottomSheet(
            sheetState = categorySheetState,
            categories = currentCategories,
            selectedCategory = category,
            transactionType = templateType,
            onSelectCategory = { cat ->
                category = cat.name
            },
            onNavigateToCreateCategory = {
                showCategorySheet = false
                onNavigateToCreateCategory(templateType)
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    // Delete Confirmation Dialog (Destructive Always AlertDialog)
    if (showDeleteConfirm && templateId != null) {
        ConfirmDialog(
            title = "Delete Template?",
            message = "This template will be removed. Existing transactions created with it will remain unaffected.",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteTemplate(templateId) {
                    onNavigateBack()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // Discard Confirmation (Non-destructive Bottom Sheet)
    if (showDiscardConfirm) {
        ConfirmationBottomSheet(
            title = "Discard Changes?",
            message = "You have unsaved template changes. Are you sure you want to discard them?",
            confirmButtonText = "Discard",
            onConfirm = {
                showDiscardConfirm = false
                onNavigateBack()
            },
            onDismiss = { showDiscardConfirm = false }
        )
    }
}
