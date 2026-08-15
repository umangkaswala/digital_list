package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val OPTIONS: List<Pair<String, Int?>> = listOf(
    "No early alert" to null,
    "10 minutes before" to 10,
    "15 minutes before" to 15,
    "1 hour before" to 60,
    "1 day before" to 1440,
)

/** Screen 19. "Custom…" isn't wired to a duration picker yet — no minutes value it could set. */
@Composable
fun EarlyAlertMenu(uiState: CaptureUiState, viewModel: CaptureViewModel) {
    AlertDialog(
        onDismissRequest = viewModel::dismissEarlyAlertMenu,
        title = { Text("Early alert") },
        text = {
            Column {
                OPTIONS.forEach { (label, minutes) ->
                    val selected = uiState.draft.earlyAlertMinutes == minutes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = selected, onClick = { viewModel.selectEarlyAlert(minutes) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Text(
                    text = "Custom…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = viewModel::dismissEarlyAlertMenu) { Text("Close") } },
    )
}
