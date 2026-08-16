package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.stackpointer.list.domain.model.AlertType

private val OPTIONS = listOf(
    Triple(AlertType.SOFT, "Soft", "Silent notification only"),
    Triple(AlertType.MEDIUM, "Medium", "Sound once, then quiet"),
    Triple(AlertType.INSISTENT, "Insistent", "Repeats until you respond"),
)

/** Screen 26 (alert-type half). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertTypeSheet(uiState: CaptureUiState, viewModel: CaptureViewModel) {
    var selected by remember { mutableStateOf(uiState.draft.alertType) }

    ModalBottomSheet(
        onDismissRequest = viewModel::dismissAlertTypeSheet,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Text(
            text = "Alert type",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Column {
            OPTIONS.forEach { (alertType, label, supportingText) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected == alertType, onClick = { selected = alertType })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == alertType, onClick = { selected = alertType })
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { viewModel.setDefaultAlertType(selected) }) { Text("Set as default") }
            Button(onClick = { viewModel.setAlertType(selected) }) { Text("Done") }
        }
    }
}
