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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.ui.theme.MaliFamily
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerBottomSheet(
    sheetState: SheetState,
    initialDateTimeMillis: Long,
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val initialLocalDateTime = remember(initialDateTimeMillis) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(initialDateTimeMillis), zoneId)
    }

    var selectedDate by remember { mutableStateOf(initialLocalDateTime.toLocalDate()) }
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(initialLocalDateTime)) }

    // Time State (12-hour format)
    val initialHour24 = initialLocalDateTime.hour
    val initialIsPm = initialHour24 >= 12
    val initialHour12 = when {
        initialHour24 == 0 -> 12
        initialHour24 > 12 -> initialHour24 - 12
        else -> initialHour24
    }

    var selectedHour12 by remember { mutableIntStateOf(initialHour12) }
    var selectedMinute by remember { mutableIntStateOf(initialLocalDateTime.minute) }
    var isPm by remember { mutableStateOf(initialIsPm) }

    val today = remember { LocalDate.now(zoneId) }

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
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Date & Time",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ─── CALENDAR MONTH NAVIGATION ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${displayedYearMonth.year}",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "‹",
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "›",
                            fontFamily = MaliFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday Headers (Mon - Sun)
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                )
                daysOfWeek.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                        fontFamily = MaliFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ─── CALENDAR DAYS GRID ──────────────────────────────────────────
            val firstDayOfMonth = displayedYearMonth.atDay(1)
            val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) // 0 for Monday
            val daysInMonth = displayedYearMonth.lengthOfMonth()
            val totalCells = ((dayOfWeekOffset + daysInMonth + 6) / 7) * 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (row in 0 until totalCells / 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val dayNumber = index - dayOfWeekOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val currentDate = displayedYearMonth.atDay(dayNumber)
                                val isSelected = currentDate == selectedDate
                                val isToday = currentDate == today

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.onSurface
                                            else Color.Transparent
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.dp else 0.dp,
                                            color = if (isToday && !isSelected) MaterialTheme.colorScheme.outline
                                            else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedDate = currentDate },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontFamily = MaliFamily,
                                        fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(14.dp))

            // ─── TIME SECTION ────────────────────────────────────────────────
            Text(
                text = "Time",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Selector
                NumberStepper(
                    value = selectedHour12,
                    minValue = 1,
                    maxValue = 12,
                    formatString = "%02d",
                    onValueChange = { selectedHour12 = it }
                )

                Text(
                    text = ":",
                    fontFamily = MaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                // Minute Selector
                NumberStepper(
                    value = selectedMinute,
                    minValue = 0,
                    maxValue = 59,
                    step = 5,
                    formatString = "%02d",
                    onValueChange = { selectedMinute = it }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // AM / PM Selector
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isPm) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isPm = false }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            fontFamily = MaliFamily,
                            fontWeight = if (!isPm) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (!isPm) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isPm) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isPm = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PM",
                            fontFamily = MaliFamily,
                            fontWeight = if (isPm) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isPm) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ─── ACTION BUTTONS: CANCEL & APPLY ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        fontFamily = MaliFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                MonochromePrimaryButton(
                    text = "Apply",
                    onClick = {
                        val hour24 = when {
                            isPm && selectedHour12 < 12 -> selectedHour12 + 12
                            !isPm && selectedHour12 == 12 -> 0
                            else -> selectedHour12
                        }
                        val localTime = LocalTime.of(hour24, selectedMinute)
                        val finalDateTime = LocalDateTime.of(selectedDate, localTime)
                        val epochMillis = finalDateTime.atZone(zoneId).toInstant().toEpochMilli()
                        onDateTimeSelected(epochMillis)
                        onDismiss()
                    },
                    modifier = Modifier.width(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumberStepper(
    value: Int,
    minValue: Int,
    maxValue: Int,
    step: Int = 1,
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
                val newVal = value - step
                onValueChange(if (newVal < minValue) maxValue else newVal)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Text(
                text = "−",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = String.format(Locale.getDefault(), formatString, value),
            fontFamily = MaliFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = {
                val newVal = value + step
                onValueChange(if (newVal > maxValue) minValue else newVal)
            },
            modifier = Modifier.size(30.dp)
        ) {
            Text(
                text = "+",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
