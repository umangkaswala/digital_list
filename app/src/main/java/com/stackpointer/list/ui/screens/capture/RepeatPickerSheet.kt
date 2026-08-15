package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.RecurrenceFreq
import com.stackpointer.list.ui.screens.common.ItemFormatting
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/** Screen 25. "Custom interval…" and refining "Ends" aren't wired to sub-pickers yet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatPickerSheet(uiState: CaptureUiState, viewModel: CaptureViewModel) {
    val zone = ZoneId.systemDefault()
    val referenceDate = (uiState.draft.dueAt ?: Instant.now()).atZone(zone).toLocalDate()

    var selectedFreq by remember { mutableStateOf(uiState.draft.recurrence?.freq) }
    var selectedWeekdays by remember {
        mutableStateOf(uiState.draft.recurrence?.weekdays?.takeIf { it.isNotEmpty() } ?: setOf(referenceDate.dayOfWeek))
    }

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Ends", modifier = Modifier.padding(start = 12.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Never", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Filled.ChevronRight, contentDescription = null)
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
                        id = uiState.draft.recurrence?.id ?: UUID.randomUUID().toString(),
                        freq = freq,
                        weekdays = if (freq == RecurrenceFreq.WEEKLY) selectedWeekdays else emptySet(),
                        monthDay = if (freq == RecurrenceFreq.MONTHLY) referenceDate.dayOfMonth else null,
                    )
                }
                viewModel.setRecurrence(recurrence)
            }) { Text("Save") }
        }
    }
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
