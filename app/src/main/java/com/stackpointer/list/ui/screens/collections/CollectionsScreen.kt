package com.stackpointer.list.ui.screens.collections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.CollectionSummary
import com.stackpointer.list.ui.theme.DigitalListTheme

/** Screen 27 — replaces the superseded screen 07 per CLAUDE.md's "07 is superseded" note.
 * Place-collection rows are deferred (see Features.placeReminders) and not shown. */
@Composable
fun CollectionsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CollectionsContent(
        uiState = uiState,
        onBack = onBack,
        onCreate = viewModel::createCollection,
        onRename = viewModel::renameCollection,
        onDelete = viewModel::deleteCollection,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionsContent(
    uiState: CollectionsUiState,
    onBack: () -> Unit,
    onCreate: (String) -> Unit,
    onRename: (Collection, String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var createDialogOpen by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Collection?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Manage collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // The handoff shows this icon without documenting its menu contents — left
                // inert per CLAUDE.md's "ask rather than invent" rather than guessing.
                actions = { IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = "More options") } },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.collections, key = { it.collection.id }) { summary ->
                    CollectionRow(
                        summary = summary,
                        onRename = { renameTarget = summary.collection },
                        onDelete = { onDelete(summary.collection.id) },
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TextButton(onClick = { createDialogOpen = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("New collection")
                        }
                    }
                }
            }
            Text(
                text = "Shared collections can be left but not deleted. Items you remove from a " +
                    "collection stay in your list.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (createDialogOpen) {
        CollectionNameDialog(
            title = "New collection",
            initialName = "",
            onConfirm = { name -> onCreate(name); createDialogOpen = false },
            onDismiss = { createDialogOpen = false },
        )
    }
    renameTarget?.let { target ->
        CollectionNameDialog(
            title = "Rename collection",
            initialName = target.name,
            onConfirm = { name -> onRename(target, name); renameTarget = null },
            onDismiss = { renameTarget = null },
        )
    }
}

@Composable
private fun CollectionRow(summary: CollectionSummary, onRename: () -> Unit, onDelete: () -> Unit) {
    var overflowOpen by remember { mutableStateOf(false) }
    val collection = summary.collection

    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(imageVector = iconFor(collection.iconKey), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = collection.name, style = MaterialTheme.typography.bodyLarge)
            val supporting = if (collection.isShared) {
                "Shared with Priya and Sam"
            } else {
                "${summary.itemCount} items · ${summary.dueCount} due"
            }
            Text(text = supporting, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        // Drag-to-reorder isn't wired yet (see M9 polish), matching the capture sheet's
        // checklist handle — shown for visual fidelity only.
        Icon(Icons.Filled.DragIndicator, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column {
            IconButton(onClick = { overflowOpen = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More options for ${collection.name}")
            }
            DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = { overflowOpen = false; onRename() })
                if (!collection.isShared) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { overflowOpen = false; onDelete() })
                }
            }
        }
    }
}

@Composable
private fun CollectionNameDialog(title: String, initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, label = { Text("Name") })
        },
        confirmButton = {
            OutlinedButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun iconFor(iconKey: String): ImageVector = when (iconKey) {
    "work" -> Icons.Filled.Work
    "person" -> Icons.Filled.Person
    "home" -> Icons.Filled.Home
    "flight" -> Icons.Filled.Flight
    "group" -> Icons.Filled.Group
    else -> Icons.AutoMirrored.Filled.Label
}

@Preview(showBackground = true)
@Composable
private fun CollectionsScreenPreview() {
    DigitalListTheme {
        CollectionsContent(
            uiState = CollectionsUiState(
                isLoading = false,
                collections = listOf(
                    CollectionSummary(Collection("1", "Work", "work", null, false, 0), 14, 3),
                    CollectionSummary(Collection("2", "Personal", "person", null, false, 1), 21, 1),
                    CollectionSummary(Collection("3", "Home", "home", null, false, 2), 9, 0),
                    CollectionSummary(Collection("4", "Flat move", "group", null, true, 3), 4, 0),
                ),
            ),
            onBack = {},
            onCreate = {},
            onRename = { _, _ -> },
            onDelete = {},
        )
    }
}
