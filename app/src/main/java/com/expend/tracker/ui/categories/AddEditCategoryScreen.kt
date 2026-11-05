package com.expend.tracker.ui.categories

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.components.MonochromeSegmentedControl
import com.expend.tracker.ui.components.MonochromeTextField
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.viewmodel.ExpendViewModel

@Composable
fun AddEditCategoryScreen(
    viewModel: ExpendViewModel,
    categoryId: Long? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = categoryId != null && categoryId > 0L

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var originalName by remember { mutableStateOf("") }
    var originalType by remember { mutableStateOf(initialType) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiscardConfirm by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val hasUnsavedChanges = name.trim() != originalName.trim() || selectedType != originalType

    BackHandler {
        if (hasUnsavedChanges && !isSubmitting) {
            showDiscardConfirm = true
        } else {
            onNavigateBack()
        }
    }

    LaunchedEffect(categoryId) {
        if (isEditing) {
            val cat = viewModel.getCategory(categoryId!!)
            if (cat != null) {
                name = cat.name
                val type = TransactionType.fromString(cat.type)
                selectedType = type
                originalName = cat.name
                originalType = type
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
        // ─── CLEAN HEADER (No Back Icon) ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Edit Category" else "Create Category",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
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
            },
            label = "Category Name",
            placeholder = "e.g., Subscriptions, Books, Investment"
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
            text = if (isEditing) "Save Changes" else "Save Category",
            enabled = !isSubmitting,
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) {
                    errorMessage = "Please enter a category name."
                    return@MonochromePrimaryButton
                }

                isSubmitting = true
                viewModel.saveCategory(
                    id = if (isEditing) categoryId!! else 0L,
                    name = trimmed,
                    type = selectedType.name,
                    onComplete = {
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
