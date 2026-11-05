package com.expend.tracker.ui.categories

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.expend.tracker.data.entities.CategoryEntity
import com.expend.tracker.data.models.CategoryIconRegistry
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.IconPickerBottomSheet
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeSegmentedControl
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.viewmodel.ExpendViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    viewModel: ExpendViewModel,
    categoryId: Long? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit,
    onCategorySaved: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isEditing = categoryId != null && categoryId > 0L
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedIconId by remember { mutableStateOf(CategoryIconRegistry.DEFAULT_ICON_ID) }
    var isIconManuallySelected by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    var originalName by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf(initialType) }
    var originalIconId by remember { mutableStateOf(CategoryIconRegistry.DEFAULT_ICON_ID) }
    var loadedCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val hasUnsavedChanges = name.trim() != originalName.trim() || selectedType != originalType || selectedIconId != originalIconId

    BackHandler {
        when {
            showIconPicker -> showIconPicker = false
            showDeleteConfirm -> showDeleteConfirm = false
            showDiscardConfirm -> showDiscardConfirm = false
            hasUnsavedChanges && !isSubmitting -> showDiscardConfirm = true
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(categoryId) {
        if (isEditing) {
            val cat = viewModel.getCategory(categoryId!!)
            if (cat != null) {
                loadedCategory = cat
                name = cat.name
                val type = TransactionType.fromString(cat.type)
                selectedType = type
                selectedIconId = cat.iconId
                isIconManuallySelected = true
                originalName = cat.name
                originalType = type
                originalIconId = cat.iconId
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── HEADER (With Top-Right Delete when editing) ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Category" else "Create Category",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isEditing) {
                TextButton(onClick = {
                    val cat = loadedCategory
                    if (cat != null) {
                        scope.launch {
                            val count = viewModel.getTransactionCountForCategory(cat.name)
                            if (count > 0) {
                                errorMessage = "Cannot delete category '${cat.name}' because it is in use by $count transaction(s)."
                            } else {
                                showDeleteConfirm = true
                            }
                        }
                    }
                }) {
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

        Spacer(modifier = Modifier.height(8.dp))

        // ─── TYPE SELECTOR ───────────────────────────────────────────────────
        MonochromeSegmentedControl(
            options = listOf("Expense", "Income"),
            selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
            onOptionSelected = { index ->
                selectedType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
            }
        )

        // ─── CATEGORY NAME INPUT ─────────────────────────────────────────────
        MonochromeTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
                if (!isIconManuallySelected && it.isNotBlank()) {
                    selectedIconId = CategoryIconRegistry.defaultIconForName(it)
                }
            },
            label = "Category Name",
            placeholder = "e.g., Subscriptions, Books, Investment"
        )

        // ─── CATEGORY ICON PICKER ROW ────────────────────────────────────────
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
                        val trimmed = name.trim()
                        if (trimmed.isEmpty()) {
                            errorMessage = "Please enter a category name."
                            return@MonochromePrimaryButton
                        }

                        isSubmitting = true
                        viewModel.saveCategory(
                            id = categoryId!!,
                            name = trimmed,
                            type = selectedType.name,
                            iconId = selectedIconId,
                            onComplete = { savedName ->
                                onCategorySaved?.invoke(savedName)
                                onNavigateBack()
                            },
                            onError = { err ->
                                isSubmitting = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MonochromePrimaryButton(
                text = "Save",
                enabled = !isSubmitting,
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isEmpty()) {
                        errorMessage = "Please enter a category name."
                        return@MonochromePrimaryButton
                    }

                    isSubmitting = true
                    viewModel.saveCategory(
                        id = 0L,
                        name = trimmed,
                        type = selectedType.name,
                        iconId = selectedIconId,
                        onComplete = { savedName ->
                            onCategorySaved?.invoke(savedName)
                            onNavigateBack()
                        },
                        onError = { err ->
                            isSubmitting = false
                            errorMessage = err
                        }
                    )
                }
            )
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

    // ─── DELETE CATEGORY CONFIRMATION ────────────────────────────────────────
    if (showDeleteConfirm && loadedCategory != null) {
        ConfirmDialog(
            title = "Delete Category?",
            message = "Are you sure you want to permanently delete '${loadedCategory!!.name}'?",
            confirmButtonText = "Delete",
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteCategorySafely(
                    category = loadedCategory!!,
                    onSuccess = { onNavigateBack() },
                    onError = { errorMessage = it }
                )
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    // ─── DISCARD CHANGES CONFIRMATION ────────────────────────────────────────
    if (showDiscardConfirm) {
        ConfirmDialog(
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
