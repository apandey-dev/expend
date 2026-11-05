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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.ui.theme.MaliFamily
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour24 = initialTime.hour
    val initialIsPm = initialHour24 >= 12
    val initialHour12 = when {
        initialHour24 == 0 -> 12
        initialHour24 > 12 -> initialHour24 - 12
        else -> initialHour24
    }

    var selectedHour12 by remember { mutableIntStateOf(initialHour12) }
    var selectedMinute by remember { mutableIntStateOf(initialTime.minute) }
    var isPm by remember { mutableStateOf(initialIsPm) }

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
            val previewHour24 = when {
                isPm && selectedHour12 < 12 -> selectedHour12 + 12
                !isPm && selectedHour12 == 12 -> 0
                else -> selectedHour12
            }
            val previewTime = LocalTime.of(previewHour24, selectedMinute)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Time",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = previewTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                    fontFamily = MaliFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Time Selector: Hour : Minute  [AM / PM]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Stepper
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hour",
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TimeNumberStepper(
                        value = selectedHour12,
                        minValue = 1,
                        maxValue = 12,
                        formatString = "%02d",
                        onValueChange = { selectedHour12 = it }
                    )
                }

                Text(
                    text = ":",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .padding(top = 18.dp)
                )

                // Minute Stepper
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Minute",
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TimeNumberStepper(
                        value = selectedMinute,
                        minValue = 0,
                        maxValue = 59,
                        formatString = "%02d",
                        onValueChange = { selectedMinute = it }
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // AM / PM Toggle
                Column(
                    modifier = Modifier.padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val amShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    val pmShape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)

                    // AM
                    Box(
                        modifier = Modifier
                            .clip(amShape)
                            .background(
                                if (!isPm) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { isPm = false }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "AM",
                            fontFamily = MaliFamily,
                            fontWeight = if (!isPm) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (!isPm) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // PM
                    Box(
                        modifier = Modifier
                            .clip(pmShape)
                            .background(
                                if (isPm) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { isPm = true }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "PM",
                            fontFamily = MaliFamily,
                            fontWeight = if (isPm) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isPm) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

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
                    text = "Apply",
                    onClick = {
                        val finalHour24 = when {
                            isPm && selectedHour12 < 12 -> selectedHour12 + 12
                            !isPm && selectedHour12 == 12 -> 0
                            else -> selectedHour12
                        }
                        val finalTime = LocalTime.of(finalHour24, selectedMinute)
                        onTimeSelected(finalTime)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeNumberStepper(
    value: Int,
    minValue: Int,
    maxValue: Int,
    formatString: String = "%d",
    onValueChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        IconButton(
            onClick = {
                val newVal = value - 1
                onValueChange(if (newVal < minValue) maxValue else newVal)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "−",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = String.format(Locale.getDefault(), formatString, value),
            fontFamily = MaliFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = {
                val newVal = value + 1
                onValueChange(if (newVal > maxValue) minValue else newVal)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Text(
                text = "+",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
