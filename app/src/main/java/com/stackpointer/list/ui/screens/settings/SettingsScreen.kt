package com.stackpointer.list.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Settings
import com.stackpointer.list.domain.model.ThemeMode
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Screen 30. Account/sync and the deferred alert rows are shown disabled per
 * [com.stackpointer.list.Features], per the design's own "deferred, disable" annotations. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsContent(
        uiState = uiState,
        onBack = onBack,
        onOpenRecycleBin = onOpenRecycleBin,
        onSetThemeMode = viewModel::setThemeMode,
        onSetShowPresetTimes = viewModel::setShowPresetTimes,
        onSetShowPresetPlaces = viewModel::setShowPresetPlaces,
        onSetDefaultAlertType = viewModel::setDefaultAlertType,
        onSetAllDayAlertTime = viewModel::setAllDayAlertTime,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetShowPresetTimes: (Boolean) -> Unit,
    onSetShowPresetPlaces: (Boolean) -> Unit,
    onSetDefaultAlertType: (AlertType) -> Unit,
    onSetAllDayAlertTime: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var themeDialogOpen by remember { mutableStateOf(false) }
    var alertTypeDialogOpen by remember { mutableStateOf(false) }
    var allDayTimeDialogOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            SettingsGroup(label = null) {
                SettingsRow("Account and sync", "a.mehta@gmail.com · synced 2 minutes ago", enabled = false)
                SettingsRow("Sync on mobile data", "Off — syncs on Wi-Fi only", enabled = false)
            }

            SettingsGroup(label = "PRESETS") {
                SwitchRow("Show preset times", checked = uiState.settings.showPresetTimes, onCheckedChange = onSetShowPresetTimes)
                SwitchRow("Show preset places", checked = uiState.settings.showPresetPlaces, onCheckedChange = onSetShowPresetPlaces)
            }

            SettingsGroup(label = "ALERTS") {
                SettingsRow(
                    title = "Default alert type",
                    supporting = uiState.settings.defaultAlertType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { alertTypeDialogOpen = true },
                )
                SettingsRow(
                    title = "Alert time for all-day items",
                    supporting = "On the day at ${formatTime(uiState.settings.allDayAlertHour, uiState.settings.allDayAlertMinute)}",
                    onClick = { allDayTimeDialogOpen = true },
                )
                SwitchRow(
                    title = "Dismiss on all devices",
                    supporting = "Dismissing an alert here dismisses it everywhere.",
                    checked = false,
                    onCheckedChange = {},
                    enabled = false,
                )
                SwitchRow(
                    title = "Shared collection alerts",
                    supporting = "Tell me when someone adds or completes an item.",
                    checked = false,
                    onCheckedChange = {},
                    enabled = false,
                )
            }

            SettingsGroup(label = "GENERAL") {
                SettingsRow(
                    title = "Recycle bin",
                    supporting = "${uiState.recycleBinCount} items",
                    onClick = onOpenRecycleBin,
                )
                SettingsRow(
                    title = "Theme",
                    supporting = uiState.settings.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = { themeDialogOpen = true },
                )
            }
        }
    }

    if (themeDialogOpen) {
        RadioDialog(
            title = "Theme",
            options = ThemeMode.entries.map { it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
            selected = uiState.settings.themeMode,
            onSelect = { onSetThemeMode(it); themeDialogOpen = false },
            onDismiss = { themeDialogOpen = false },
        )
    }
    if (alertTypeDialogOpen) {
        RadioDialog(
            title = "Default alert type",
            options = listOf(
                AlertType.SOFT to "Soft",
                AlertType.MEDIUM to "Medium",
                AlertType.INSISTENT to "Insistent",
            ),
            selected = uiState.settings.defaultAlertType,
            onSelect = { onSetDefaultAlertType(it); alertTypeDialogOpen = false },
            onDismiss = { alertTypeDialogOpen = false },
        )
    }
    if (allDayTimeDialogOpen) {
        AllDayAlertTimeDialog(
            initialHour = uiState.settings.allDayAlertHour,
            initialMinute = uiState.settings.allDayAlertMinute,
            onConfirm = { hour, minute -> onSetAllDayAlertTime(hour, minute); allDayTimeDialogOpen = false },
            onDismiss = { allDayTimeDialogOpen = false },
        )
    }
}

@Composable
private fun SettingsGroup(label: String?, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp),
            )
        }
        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(title: String, supporting: String, onClick: (() -> Unit)? = null, enabled: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null && enabled) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    supporting: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun <T> RadioDialog(title: String, options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected == value, onClick = { onSelect(value) })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == value, onClick = { onSelect(value) })
                        Text(text = label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllDayAlertTimeDialog(initialHour: Int, initialMinute: Int, onConfirm: (Int, Int) -> Unit, onDismiss: () -> Unit) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = false)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = "Alert time for all-day items", style = MaterialTheme.typography.titleMedium)
                TimePicker(state = state, modifier = Modifier.padding(top = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = { onConfirm(state.hour, state.minute) }, modifier = Modifier.padding(start = 8.dp)) { Text("Save") }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("h:mm a", Locale.US))

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DigitalListTheme {
        SettingsContent(
            uiState = SettingsUiState(isLoading = false, settings = Settings(), recycleBinCount = 3),
            onBack = {},
            onOpenRecycleBin = {},
            onSetThemeMode = {},
            onSetShowPresetTimes = {},
            onSetShowPresetPlaces = {},
            onSetDefaultAlertType = {},
            onSetAllDayAlertTime = { _, _ -> },
        )
    }
}
