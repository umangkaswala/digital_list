package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.ui.screens.common.ItemFormatting
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Screen 18. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeModeContent(uiState: CaptureUiState, viewModel: CaptureViewModel, modifier: Modifier = Modifier) {
    val draft = uiState.draft
    val zone = ZoneId.systemDefault()
    val now = Instant.now()

    Column(modifier = modifier.fillMaxWidth()) {
        if (draft.dueAt != null) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                InputChip(
                    selected = false,
                    onClick = {},
                    label = {
                        val recurrenceSuffix = draft.recurrence?.let { " · ${ItemFormatting.recurrenceText(it)}" }.orEmpty()
                        Text(ItemFormatting.dueText(draft.dueAt, now, zone) + recurrenceSuffix)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "Remove trigger",
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        ListItem(
            headlineContent = { Text("All day") },
            leadingContent = { Icon(Icons.Filled.HourglassEmpty, contentDescription = null) },
            trailingContent = { Switch(checked = draft.isAllDay, onCheckedChange = viewModel::setAllDay) },
        )

        ListItem(
            headlineContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(onClick = viewModel::openDatePicker) {
                        Text(
                            (draft.dueAt ?: now).atZone(zone).toLocalDate().toString(),
                        )
                    }
                    if (!draft.isAllDay) {
                        TextButton(onClick = viewModel::openTimePicker) {
                            Text(ItemFormatting.timeOnly(draft.dueAt ?: now, zone))
                        }
                    }
                }
            },
            leadingContent = { Icon(Icons.Filled.Event, contentDescription = null) },
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        ) {
            items(presets(now, zone)) { (label, instant) ->
                AssistChip(onClick = { viewModel.applyPreset(instant) }, label = { Text(label) })
            }
        }

        ListItem(
            modifier = Modifier.clickable(onClick = viewModel::openEarlyAlertMenu),
            headlineContent = { Text(earlyAlertLabel(draft.earlyAlertMinutes)) },
            leadingContent = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )

        ListItem(
            modifier = Modifier.clickable(onClick = viewModel::openRepeatPicker),
            headlineContent = {
                Text(draft.recurrence?.let { ItemFormatting.recurrenceText(it) } ?: "Never")
            },
            leadingContent = { Icon(Icons.Filled.Repeat, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
        )

        ListItem(
            modifier = Modifier.clickable(onClick = viewModel::openAlertTypeSheet),
            headlineContent = { Text("Alert · ${alertTypeLabel(draft.alertType)}") },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
        )
    }

    if (uiState.datePickerOpen) {
        val initialDate = (draft.dueAt ?: now).atZone(zone).toLocalDate()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = viewModel::dismissDatePicker,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        viewModel.setDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    viewModel.dismissDatePicker()
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDatePicker) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    if (uiState.timePickerOpen) {
        val initialTime = (draft.dueAt ?: now).atZone(zone).toLocalTime()
        val state = rememberTimePickerState(initialHour = initialTime.hour, initialMinute = initialTime.minute)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = viewModel::dismissTimePicker,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setTime(LocalTime.of(state.hour, state.minute))
                    viewModel.dismissTimePicker()
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissTimePicker) { Text("Cancel") } },
            text = { TimePicker(state = state) },
        )
    }
}

private fun presets(now: Instant, zone: ZoneId): List<Pair<String, Instant>> {
    val today = now.atZone(zone).toLocalDate()
    return listOf(
        "In an hour" to now.plusSeconds(3600),
        "7:00 AM" to today.atTime(7, 0).atZone(zone).toInstant(),
        "3:00 PM" to today.atTime(15, 0).atZone(zone).toInstant(),
        "10:00 PM" to today.atTime(22, 0).atZone(zone).toInstant(),
    )
}

private fun earlyAlertLabel(minutes: Int?): String = when (minutes) {
    null -> "No early alert"
    10 -> "10 minutes before"
    15 -> "15 minutes before"
    60 -> "1 hour before"
    1440 -> "1 day before"
    else -> "$minutes minutes before"
}

private fun alertTypeLabel(alertType: AlertType): String = when (alertType) {
    AlertType.SOFT -> "Soft"
    AlertType.MEDIUM -> "Medium"
    AlertType.INSISTENT -> "Insistent"
}
