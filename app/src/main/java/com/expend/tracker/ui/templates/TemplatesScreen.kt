package com.expend.tracker.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.util.CurrencyFormatter
import com.expend.tracker.viewmodel.ExpendViewModel

@Composable
fun TemplatesScreen(
    viewModel: ExpendViewModel,
    onNavigateToAddTemplate: () -> Unit,
    onNavigateToEditTemplate: (Long) -> Unit,
    onUseTemplate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.templates.collectAsState()
    var templateToDelete by remember { mutableStateOf<TemplateEntity?>(null) }

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
                        onUse = { onUseTemplate(template.id) },
                        onEdit = { onNavigateToEditTemplate(template.id) },
                        onDelete = { templateToDelete = template }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
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
 * Slightly larger Template Card with 3 consistent text-only actions:
 * [ Use Template ]  [ Edit ]  [ Delete ]
 */
@Composable
private fun TemplateItemCard(
    template: TemplateEntity,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = template.type == TransactionType.INCOME.name

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        fontFamily = MaliFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            // ─── THREE TEXT ACTIONS IN ONE ROW: [Use Template] [Edit] [Delete] ───
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Primary: Use Template
                MonochromePrimaryButton(
                    text = "Use Template",
                    onClick = onUse,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                )

                // 2. Edit (Container)
                MonochromeOutlinedButton(
                    text = "Edit",
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(38.dp)
                )

                // 3. Delete (Container)
                MonochromeOutlinedButton(
                    text = "Delete",
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(0.85f)
                        .height(38.dp)
                )
            }
        }
    }
}
