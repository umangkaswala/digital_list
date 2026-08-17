package com.stackpointer.list.ui.screens.capture

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private val SubItemRowHeight = 48.dp

/** Screen 22 (checklist part only — image attachments are deferred, see Features.imageAttachments). */
@Composable
fun ChecklistModeContent(uiState: CaptureUiState, viewModel: CaptureViewModel, modifier: Modifier = Modifier) {
    val subItems = uiState.draft.subItems.sortedBy { it.sortOrder }
    val currentSubItems by rememberUpdatedState(subItems)
    val density = LocalDensity.current
    val rowHeightPx = with(density) { SubItemRowHeight.toPx() }

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (subItems.isNotEmpty()) {
            Text(
                text = "${subItems.size} items",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        subItems.forEach { subItem ->
            val isDragging = subItem.id == draggingId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SubItemRowHeight)
                    .then(
                        if (isDragging) {
                            Modifier.zIndex(1f).offset { IntOffset(0, dragOffsetY.roundToInt()) }
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 8.dp),
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
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Reorder item",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.pointerInput(subItem.id) {
                        detectDragGestures(
                            onDragStart = {
                                draggingId = subItem.id
                                dragOffsetY = 0f
                            },
                            onDragEnd = {
                                draggingId = null
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                draggingId = null
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                                val list = currentSubItems
                                val fromIndex = list.indexOfFirst { it.id == subItem.id }
                                if (fromIndex == -1) return@detectDragGestures
                                val steps = (dragOffsetY / rowHeightPx).roundToInt()
                                val toIndex = (fromIndex + steps).coerceIn(0, list.lastIndex)
                                if (toIndex != fromIndex) {
                                    viewModel.moveSubItem(subItem.id, toIndex)
                                    dragOffsetY -= (toIndex - fromIndex) * rowHeightPx
                                }
                            },
                        )
                    },
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
