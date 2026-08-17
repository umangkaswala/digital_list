package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceEndType
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.ui.screens.common.ItemFormatting
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.UUID

/** Screen 25. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatPickerSheet(uiState: CaptureUiState, viewModel: CaptureViewModel) {
    val zone = ZoneId.systemDefault()
    val referenceDate = (uiState.draft.dueAt ?: Instant.now()).atZone(zone).toLocalDate()
    val existing = uiState.draft.recurrence

    var selectedFreq by remember { mutableStateOf(existing?.freq) }
    var selectedWeekdays by remember {
        mutableStateOf(existing?.weekdays?.takeIf { it.isNotEmpty() } ?: setOf(referenceDate.dayOfWeek))
    }
    var customInterval by remember {
        mutableIntStateOf(existing?.interval?.takeIf { existing.freq == RecurrenceFreq.CUSTOM } ?: 2)
    }
    var endType by remember { mutableStateOf(existing?.endType ?: RecurrenceEndType.NEVER) }
    var endDate by remember { mutableStateOf(existing?.endDate ?: Instant.now()) }
    var endCount by remember { mutableIntStateOf(existing?.endCount ?: 5) }

    var endsMenuOpen by remember { mutableStateOf(false) }
    var endsDatePickerOpen by remember { mutableStateOf(false) }
    var endsCountDialogOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = viewModel::dismissRepeatPicker,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Text(
            text = "Repeat",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        RepeatOption("Never", selectedFreq == null) { selectedFreq = null }
        RepeatOption("Every day", selectedFreq == RecurrenceFreq.DAILY) { selectedFreq = RecurrenceFreq.DAILY }
        RepeatOption("Every week", selectedFreq == RecurrenceFreq.WEEKLY) { selectedFreq = RecurrenceFreq.WEEKLY }
        if (selectedFreq == RecurrenceFreq.WEEKLY) {
            WeekdayToggleRow(
                selected = selectedWeekdays,
                onToggle = { day ->
                    selectedWeekdays = if (day in selectedWeekdays) {
                        (selectedWeekdays - day).ifEmpty { setOf(day) }
                    } else {
                        selectedWeekdays + day
                    }
                },
            )
        }
        RepeatOption(
            "Every month on the ${ItemFormatting.ordinal(referenceDate.dayOfMonth)}",
            selectedFreq == RecurrenceFreq.MONTHLY,
        ) { selectedFreq = RecurrenceFreq.MONTHLY }
        val monthName = referenceDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        RepeatOption(
            "Every year on $monthName ${referenceDate.dayOfMonth}",
            selectedFreq == RecurrenceFreq.YEARLY,
        ) { selectedFreq = RecurrenceFreq.YEARLY }
        RepeatOption("Custom interval…", selectedFreq == RecurrenceFreq.CUSTOM) { selectedFreq = RecurrenceFreq.CUSTOM }
        if (selectedFreq == RecurrenceFreq.CUSTOM) {
            IntervalStepperRow(
                value = customInterval,
                unitLabel = if (customInterval == 1) "day" else "days",
                onChange = { customInterval = it.coerceAtLeast(1) },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = false, onClick = { endsMenuOpen = true })
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Ends", modifier = Modifier.padding(start = 12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = endsSummary(endType, endDate, endCount, zone),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
            }
            DropdownMenu(expanded = endsMenuOpen, onDismissRequest = { endsMenuOpen = false }) {
                DropdownMenuItem(text = { Text("Never") }, onClick = {
                    endType = RecurrenceEndType.NEVER
                    endsMenuOpen = false
                })
                DropdownMenuItem(text = { Text("On date") }, onClick = {
                    endsMenuOpen = false
                    endsDatePickerOpen = true
                })
                DropdownMenuItem(text = { Text("After number of times") }, onClick = {
                    endsMenuOpen = false
                    endsCountDialogOpen = true
                })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = viewModel::dismissRepeatPicker) { Text("Cancel") }
            TextButton(onClick = {
                val freq = selectedFreq
                val recurrence = if (freq == null) {
                    null
                } else {
                    Recurrence(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        freq = freq,
                        interval = if (freq == RecurrenceFreq.CUSTOM) customInterval else 1,
                        weekdays = if (freq == RecurrenceFreq.WEEKLY) selectedWeekdays else emptySet(),
                        monthDay = if (freq == RecurrenceFreq.MONTHLY) referenceDate.dayOfMonth else null,
                        endType = endType,
                        endDate = if (endType == RecurrenceEndType.ON_DATE) endDate else null,
                        endCount = if (endType == RecurrenceEndType.AFTER_COUNT) endCount else null,
                    )
                }
                viewModel.setRecurrence(recurrence)
            }) { Text("Save") }
        }
    }

    if (endsDatePickerOpen) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = endDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { endsDatePickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = Instant.ofEpochMilli(it) }
                    endType = RecurrenceEndType.ON_DATE
                    endsDatePickerOpen = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { endsDatePickerOpen = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (endsCountDialogOpen) {
        var count by remember { mutableIntStateOf(endCount) }
        AlertDialog(
            onDismissRequest = { endsCountDialogOpen = false },
            title = { Text("Ends after") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = { count = (count - 1).coerceAtLeast(1) }) {
                        Icon(Icons.Filled.Remove, contentDescription = "Fewer occurrences")
                    }
                    Text(
                        text = if (count == 1) "1 occurrence" else "$count occurrences",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    IconButton(onClick = { count += 1 }) {
                        Icon(Icons.Filled.Add, contentDescription = "More occurrences")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    endCount = count
                    endType = RecurrenceEndType.AFTER_COUNT
                    endsCountDialogOpen = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { endsCountDialogOpen = false }) { Text("Cancel") } },
        )
    }
}

private fun endsSummary(endType: RecurrenceEndType, endDate: Instant, endCount: Int, zone: ZoneId): String = when (endType) {
    RecurrenceEndType.NEVER -> "Never"
    RecurrenceEndType.ON_DATE -> {
        val date = endDate.atZone(zone).toLocalDate()
        val monthName = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$monthName ${date.dayOfMonth}"
    }
    RecurrenceEndType.AFTER_COUNT -> if (endCount == 1) "After 1 time" else "After $endCount times"
}

@Composable
private fun RepeatOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun WeekdayToggleRow(selected: Set<DayOfWeek>, onToggle: (DayOfWeek) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DayOfWeek.entries.forEach { day ->
            FilterChip(
                selected = day in selected,
                onClick = { onToggle(day) },
                label = {
                    Text(
                        text = day.name.take(1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(0.dp),
                    )
                },
            )
        }
    }
}

/** The "Custom interval…" sub-picker — freq is always CUSTOM here, and [RecurrenceNextOccurrence]
 * only ever treats CUSTOM's interval as a day count, so this stepper has no separate unit choice. */
@Composable
private fun IntervalStepperRow(value: Int, unitLabel: String, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Every", modifier = Modifier.padding(end = 8.dp))
        IconButton(onClick = { onChange(value - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = "Fewer days")
        }
        Text(text = value.toString(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 4.dp))
        IconButton(onClick = { onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = "More days")
        }
        Text(text = unitLabel, modifier = Modifier.padding(start = 4.dp))
    }
}
