package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/** Screen 22 (checklist part only — image attachments are deferred, see Features.imageAttachments). */
@Composable
fun ChecklistModeContent(uiState: CaptureUiState, viewModel: CaptureViewModel, modifier: Modifier = Modifier) {
    val subItems = uiState.draft.subItems

    Column(modifier = modifier.fillMaxWidth()) {
        if (subItems.isNotEmpty()) {
            Text(
                text = "${subItems.size} items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        subItems.sortedBy { it.sortOrder }.forEach { subItem ->
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = subItem.isCompleted, onCheckedChange = { viewModel.toggleSubItem(subItem.id) })
                TextField(
                    value = subItem.text,
                    onValueChange = { viewModel.updateSubItemText(subItem.id, it) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { viewModel.removeSubItem(subItem.id) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove item", modifier = Modifier.size(18.dp))
                }
                // Drag-to-reorder isn't wired yet (see M9 polish) — the handle is shown for
                // visual fidelity with screen 22 but doesn't respond to drag gestures.
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AddSubItemRow(onAdd = viewModel::addSubItem)
    }
}

@Composable
private fun AddSubItemRow(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Add an item") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onAdd(text)
                text = ""
            }),
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
    }
}
