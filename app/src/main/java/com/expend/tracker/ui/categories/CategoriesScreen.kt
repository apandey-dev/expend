package com.expend.tracker.ui.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.models.CategoryIconRegistry
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmationBottomSheet
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.IconPickerBottomSheet
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeSegmentedControl
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.viewmodel.ExpendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddCategory: (TransactionType) -> Unit = {},
    onNavigateToEditCategory: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(TransactionType.EXPENSE) }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()

    val currentCategories = if (selectedTab == TransactionType.EXPENSE) {
        expenseCategories
    } else {
        incomeCategories
    }

    // State for bottom sheets & dialogs
    var selectedCategoryForActions by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryForEditSheet by remember { mutableStateOf<CategoryEntity?>(null) }
    var isAddingNewCategory by remember { mutableStateOf(false) }

    var categoryToArchiveConfirm by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDeleteConfirm by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToReassign by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteTransactionCount by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val actionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ─── HEADER ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Categories",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Organize transactions by type",
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── ADD CATEGORY BUTTON (Text-only, opens Bottom Sheet Form) ───────
        MonochromePrimaryButton(
            text = "Add Category",
            onClick = { isAddingNewCategory = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ─── EXPENSE / INCOME SEGMENTED TOGGLE ──────────────────────────────
        MonochromeSegmentedControl(
            options = listOf("Expense", "Income"),
            selectedIndex = if (selectedTab == TransactionType.EXPENSE) 0 else 1,
            onOptionSelected = { index ->
                selectedTab = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ─── CATEGORY LIST / EMPTY STATE ────────────────────────────────────
        if (currentCategories.isEmpty()) {
            MinimalEmptyState(
                message = "No ${selectedTab.title.lowercase()} categories found. Add categories to classify your transactions.",
                actionText = "Add Category",
                onActionClick = { isAddingNewCategory = true }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentCategories, key = { it.id }) { category ->
                    CleanCategoryTile(
                        category = category,
                        onLongClick = { selectedCategoryForActions = category }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // ─── CATEGORY ACTIONS BOTTOM SHEET (Triggered on Long Press) ─────────────
    if (selectedCategoryForActions != null) {
        val cat = selectedCategoryForActions!!
        CategoryActionsBottomSheet(
            sheetState = actionsSheetState,
            category = cat,
            onEdit = {
                selectedCategoryForActions = null
                categoryForEditSheet = cat
            },
            onArchive = {
                selectedCategoryForActions = null
                categoryToArchiveConfirm = cat
            },
            onDelete = {
                selectedCategoryForActions = null
                coroutineScope.launch {
                    val count = viewModel.getTransactionCountForCategory(cat.name)
                    deleteTransactionCount = count
                    if (count == 0) {
                        categoryToDeleteConfirm = cat
                    } else {
                        categoryToReassign = cat
                    }
                }
            },
            onDismiss = { selectedCategoryForActions = null }
        )
    }

    // ─── CATEGORY EDIT / CREATE MODAL BOTTOM SHEET ───────────────────────────
    if (categoryForEditSheet != null || isAddingNewCategory) {
        val existing = categoryForEditSheet
        CategoryEditBottomSheet(
            sheetState = editSheetState,
            category = existing,
            initialType = if (existing != null) TransactionType.fromString(existing.type) else selectedTab,
            onSave = { name, type, iconId ->
                viewModel.saveCategory(
                    name = name,
                    type = type.name,
                    iconId = iconId,
                    id = existing?.id ?: 0L,
                    onComplete = {
                        categoryForEditSheet = null
                        isAddingNewCategory = false
                    },
                    onError = { err ->
                        errorMessage = err
                    }
                )
            },
            onDismiss = {
                categoryForEditSheet = null
                isAddingNewCategory = false
            }
        )
    }

    // ─── ARCHIVE CATEGORY CONFIRMATION (Non-Destructive Bottom Sheet) ─────────
    if (categoryToArchiveConfirm != null) {
        val cat = categoryToArchiveConfirm!!
        val isArchived = cat.isArchived
        ConfirmationBottomSheet(
            title = if (isArchived) "Unarchive Category?" else "Archive Category?",
            message = if (isArchived)
                "Unarchiving '${cat.name}' will make it selectable again for new transactions."
            else
                "Archiving '${cat.name}' will preserve all past transactions, but it will no longer appear when selecting categories for new transactions.",
            confirmButtonText = if (isArchived) "Unarchive" else "Archive",
            onConfirm = {
                if (isArchived) {
                    viewModel.unarchiveCategory(cat.id)
                } else {
                    viewModel.archiveCategory(cat.id)
                }
                categoryToArchiveConfirm = null
            },
            onDismiss = { categoryToArchiveConfirm = null }
        )
    }

    // ─── DELETE UNUSED CATEGORY CONFIRMATION (DESTRUCTIVE MUST USE DIALOG) ───
    if (categoryToDeleteConfirm != null) {
        val cat = categoryToDeleteConfirm!!
        ConfirmDialog(
            title = "Delete Category?",
            message = "Are you sure you want to permanently delete '${cat.name}'? This category is not used by any transactions.",
            confirmButtonText = "Delete",
            onConfirm = {
                viewModel.deleteCategorySafely(
                    category = cat,
                    onSuccess = { categoryToDeleteConfirm = null },
                    onError = { err ->
                        errorMessage = err
                        categoryToDeleteConfirm = null
                    }
                )
            },
            onDismiss = { categoryToDeleteConfirm = null }
        )
    }

    // ─── REASSIGN OR ARCHIVE CATEGORY DIALOG (When Category In Use) ──────────
    if (categoryToReassign != null) {
        val catToReassign = categoryToReassign!!
        val targetCandidates = currentCategories.filter { it.id != catToReassign.id && !it.isArchived }
        ReassignOrArchiveCategoryDialog(
            category = catToReassign,
            transactionCount = deleteTransactionCount,
            candidates = targetCandidates,
            onArchive = {
                viewModel.archiveCategory(catToReassign.id)
                categoryToReassign = null
            },
            onReassignAndDelete = { targetCategoryName ->
                viewModel.reassignAndDeleteCategory(
                    oldCategory = catToReassign,
                    newCategoryName = targetCategoryName
                ) {
                    categoryToReassign = null
                }
            },
            onDismiss = { categoryToReassign = null }
        )
    }

    // ─── NOTICE / ERROR DIALOG ───────────────────────────────────────────────
    if (errorMessage != null) {
        ConfirmDialog(
            title = "Notice",
            message = errorMessage!!,
            confirmButtonText = "OK",
            onConfirm = { errorMessage = null },
            onDismiss = { errorMessage = null }
        )
    }
}

private val pastelColors = listOf(
    Color(0xFFE3F2FD), // Very light blue
    Color(0xFFE8F5E9), // Soft pastel green
    Color(0xFFFFF9C4), // Pale yellow
    Color(0xFFEDE7F6), // Light lavender
    Color(0xFFFFE0B2), // Soft peach
    Color(0xFFFCE4EC), // Pale pink
    Color(0xFFE0F2F1), // Soft mint
    Color(0xFFF3E5F5)  // Pale lilac
)

/**
 * Clean Category List Tile:
 * Strictly displays only:
 * - Category icon with subtle pastel circular background
 * - Category name
 * - Optional archived badge
 * Strictly NO visible edit, delete, archive icons or buttons.
 * Single tap: does nothing.
 * Long press: opens category actions bottom sheet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CleanCategoryTile(
    category: CategoryEntity,
    onLongClick: () -> Unit
) {
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
                onClick = {}, // Single tap intentionally does nothing per requirement
                onLongClick = onLongClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                val isDark = isSystemInDarkTheme()
                val colorIndex = kotlin.math.abs(category.name.hashCode()) % pastelColors.size
                val rawPastel = pastelColors[colorIndex]
                val circleBg = if (isDark) rawPastel.copy(alpha = 0.22f) else rawPastel

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(circleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = CategoryIconRegistry.getIconResource(category.iconId)),
                        contentDescription = category.name,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = category.name,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (category.isArchived) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Archived",
                        fontFamily = MaliFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Category Actions Bottom Sheet:
 * Triggered on Long Press of category tile.
 * Text-only action rows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryActionsBottomSheet(
    sheetState: SheetState,
    category: CategoryEntity,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
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
                text = category.name,
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

            // 1. Edit Category
            CategoryActionRow(
                text = "Edit Category",
                onClick = onEdit
            )

            // 2. Archive / Unarchive Category
            CategoryActionRow(
                text = if (category.isArchived) "Unarchive Category" else "Archive Category",
                onClick = onArchive
            )

            // 3. Delete Category
            CategoryActionRow(
                text = "Delete Category",
                onClick = onDelete
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryActionRow(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Text(
            text = text,
            fontFamily = MaliFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Dedicated Category Edit / Create Modal Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditBottomSheet(
    sheetState: SheetState,
    category: CategoryEntity?,
    initialType: TransactionType,
    onSave: (name: String, type: TransactionType, iconId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = category != null
    var name by remember(category) { mutableStateOf(category?.name ?: "") }
    var selectedType by remember(category, initialType) {
        mutableStateOf(if (category != null) TransactionType.fromString(category.type) else initialType)
    }
    var selectedIconId by remember(category) {
        mutableStateOf(category?.iconId ?: CategoryIconRegistry.DEFAULT_ICON_ID)
    }
    var isIconManuallySelected by remember { mutableStateOf(category != null) }
    var showIconPicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Category" else "Create Category",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Category Type Selector
            MonochromeSegmentedControl(
                options = listOf("Expense", "Income"),
                selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                onOptionSelected = { index ->
                    selectedType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                }
            )

            // Category Name Field
            MonochromeTextField(
                value = name,
                onValueChange = {
                    name = it
                    validationError = null
                    if (!isIconManuallySelected && it.isNotBlank()) {
                        selectedIconId = CategoryIconRegistry.defaultIconForName(it)
                    }
                },
                label = "Category Name",
                placeholder = "e.g., Groceries, Rent, Salary"
            )

            // Category Icon Selection Row
            Column {
                Text(
                    text = "Category Icon",
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { showIconPicker = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    0.5.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = CategoryIconRegistry.getIconResource(selectedIconId)),
                                contentDescription = "Selected icon",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = CategoryIconRegistry.allIcons.firstOrNull { it.id == selectedIconId }?.label ?: "General",
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Change",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    fontFamily = MaliFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Equal-Width Actions: Cancel and Save (50% / 50% split)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Save",
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isBlank()) {
                            validationError = "Category name cannot be empty."
                        } else {
                            onSave(trimmed, selectedType, selectedIconId)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showIconPicker) {
        IconPickerBottomSheet(
            selectedIconId = selectedIconId,
            onIconSelected = { iconId ->
                selectedIconId = iconId
                isIconManuallySelected = true
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

/**
 * Reassign or Archive Dialog when trying to delete a Category currently in use.
 * Retains AlertDialog format per requirement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReassignOrArchiveCategoryDialog(
    category: CategoryEntity,
    transactionCount: Int,
    candidates: List<CategoryEntity>,
    onArchive: () -> Unit,
    onReassignAndDelete: (targetCategoryName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTarget by remember { mutableStateOf(candidates.firstOrNull()?.name) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Category In Use",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "'${category.name}' is currently referenced by $transactionCount transaction(s). You can archive it or reassign its transactions to another category before deleting.",
                    fontFamily = MaliFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (candidates.isNotEmpty()) {
                    Text(
                        text = "Reassign to:",
                        fontFamily = MaliFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        items(candidates) { target ->
                            val isSelected = selectedTarget == target.name
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { selectedTarget = target.name }
                                    .padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                Text(
                                    text = target.name,
                                    fontFamily = MaliFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", fontFamily = MaliFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row {
                        TextButton(onClick = onArchive) {
                            Text("Archive", fontFamily = MaliFamily, color = MaterialTheme.colorScheme.onSurface)
                        }

                        if (selectedTarget != null) {
                            val target = selectedTarget!!
                            Spacer(modifier = Modifier.width(6.dp))
                            TextButton(onClick = { onReassignAndDelete(target) }) {
                                Text(
                                    "Reassign & Delete",
                                    fontFamily = MaliFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
