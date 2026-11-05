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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.entities.TemplateEntity
import com.expend.tracker.data.models.TransactionType
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MinimalEmptyState
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddTemplate: () -> Unit,
    onNavigateToEditTemplate: (Long) -> Unit,
    onUseTemplate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    val activeWallets by viewModel.activeWallets.collectAsState()

    var selectedTemplateForDetails by remember { mutableStateOf<TemplateEntity?>(null) }
    var templateToDelete by remember { mutableStateOf<TemplateEntity?>(null) }

    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isBackHandledLocally = selectedTemplateForDetails != null || templateToDelete != null
    BackHandler(enabled = isBackHandledLocally) {
        when {
            selectedTemplateForDetails != null -> selectedTemplateForDetails = null
            templateToDelete != null -> templateToDelete = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // ─── HEADER (No Redundant Plus Icon per Requirement 1) ───────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Templates",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Quick reuse for frequent transactions",
                    fontFamily = MaliFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── DEDICATED CREATE TEMPLATE ACTION ────────────────────────────────
        MonochromePrimaryButton(
            text = "Create Template",
            onClick = onNavigateToAddTemplate
        )

        Spacer(modifier = Modifier.height(14.dp))

        // ─── TEMPLATES LIST / EMPTY STATE ────────────────────────────────────
        if (templates.isEmpty()) {
            MinimalEmptyState(
                message = "No reusable templates yet. Create templates for recurring expenses like Rent, Groceries, or Fuel to log them with 1 tap.",
                actionText = "Create Template",
                onActionClick = onNavigateToAddTemplate
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(templates, key = { it.id }) { template ->
                    TemplateItemCard(
                        template = template,
                        onClick = { selectedTemplateForDetails = template }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // ─── TEMPLATE DETAILS BOTTOM SHEET ───────────────────────────────────────
    if (selectedTemplateForDetails != null) {
        val currentTpl = selectedTemplateForDetails!!
        val walletName = activeWallets.firstOrNull { it.id == currentTpl.defaultWalletId }?.name
        TemplateDetailsBottomSheet(
            sheetState = detailsSheetState,
            template = currentTpl,
            walletName = walletName,
            onUse = {
                val id = currentTpl.id
                selectedTemplateForDetails = null
                onUseTemplate(id)
            },
            onEdit = {
                val id = currentTpl.id
                selectedTemplateForDetails = null
                onNavigateToEditTemplate(id)
            },
            onDelete = {
                val tpl = currentTpl
                selectedTemplateForDetails = null
                templateToDelete = tpl
            },
            onDismiss = { selectedTemplateForDetails = null }
        )
    }

    // Delete Confirmation Dialog (Destructive Always AlertDialog)
    if (templateToDelete != null) {
        ConfirmDialog(
            title = "Delete Template?",
            message = "Are you sure you want to delete '${templateToDelete!!.name}'? This will not affect any transactions previously logged with it.",
            confirmButtonText = "Delete",
            onConfirm = {
                viewModel.deleteTemplate(templateToDelete!!.id)
                templateToDelete = null
            },
            onDismiss = { templateToDelete = null }
        )
    }
}

/**
 * Clean, moderately sized Template Card:
 * Displays only template information (no action buttons on tile).
 * Tapping anywhere opens the Template Details ModalBottomSheet.
 */
@Composable
private fun TemplateItemCard(
    template: TemplateEntity,
    onClick: () -> Unit
) {
    val isIncome = template.type == TransactionType.INCOME.name
    val typeLabel = if (isIncome) "Income" else "Expense"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = template.name,
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = typeLabel,
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${template.title} • ${template.category}",
                    fontFamily = MaliFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (template.defaultAmountPaise != null) {
                val amountFormatted = CurrencyFormatter.formatPaise(template.defaultAmountPaise)
                val prefix = if (isIncome) "+ " else "- "
                Text(
                    text = prefix + amountFormatted,
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
