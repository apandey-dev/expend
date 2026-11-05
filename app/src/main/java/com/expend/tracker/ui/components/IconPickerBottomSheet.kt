package com.expend.tracker.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.models.CategoryIconItem
import com.expend.tracker.data.models.CategoryIconRegistry
import com.expend.tracker.ui.theme.MaliFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    selectedIconId: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelectedId by remember { mutableStateOf(selectedIconId) }

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
                    text = "Select Category Icon",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${CategoryIconRegistry.allIcons.size} icons",
                    fontFamily = MaliFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 5-Column Grid of Icons
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                items(CategoryIconRegistry.allIcons, key = { it.id }) { item ->
                    val isSelected = item.id == currentSelectedId
                    val shape = RoundedCornerShape(10.dp)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(shape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                shape = shape
                            )
                            .clickable {
                                currentSelectedId = item.id
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            fontFamily = MaliFamily,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Equal-Width Actions: Cancel & Apply (50% / 50% split)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromeOutlinedButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                MonochromePrimaryButton(
                    text = "Select",
                    onClick = {
                        onIconSelected(currentSelectedId)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
