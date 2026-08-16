package com.stackpointer.list.ui.screens.bin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.SelectionTopBar
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme

/** Screen 29 — the recycle bin, with a selection mode scoped to just this screen (no other
 * screen has bulk selection built yet — see M7b's commit note). */
@Composable
fun BinScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BinViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    BinContent(
        uiState = uiState,
        onBack = onBack,
        onToggleSelected = viewModel::toggleSelected,
        onSelectAll = viewModel::selectAll,
        onClearSelection = viewModel::clearSelection,
        onRestore = viewModel::restoreSelected,
        onDeleteForever = viewModel::deleteSelectedForever,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BinContent(
    uiState: BinUiState,
    onBack: () -> Unit,
    onToggleSelected: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedIds.size,
                    onClose = onClearSelection,
                    actions = { TextButton(onClick = onSelectAll) { Text("Select all") } },
                )
            } else {
                TopAppBar(
                    title = { Text("Recycle bin") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                Surface(shadowElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        BinToolbarAction(Icons.Filled.RestoreFromTrash, "Restore", onRestore)
                        BinToolbarAction(Icons.Filled.DeleteForever, "Delete now", onDeleteForever)
                    }
                }
            }
        },
    ) { innerPadding ->
        if (uiState.rows.isEmpty()) {
            EmptyState(
                headline = "Nothing here yet",
                supportingText = "Items you delete stay here for 30 days before they're gone for good.",
                actionLabel = "Back",
                onAction = onBack,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            return@Scaffold
        }

        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = "Items here are deleted for good after 30 days.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                uiState.grouped.forEach { (daysLeft, rows) ->
                    item(key = "header-$daysLeft") {
                        Text(
                            text = "$daysLeft DAYS LEFT",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(rows, key = { it.item.id }) { row ->
                        BinRowItem(
                            row = row,
                            isSelected = row.item.id in uiState.selectedIds,
                            isSelectionMode = uiState.isSelectionMode,
                            onToggleSelected = { onToggleSelected(row.item.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BinRowItem(
    row: BinRow,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = row.item
    val typeLabel = when {
        item.isChecklist -> "Checklist"
        item.isNote -> "Note"
        else -> "Task"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggleSelected, onLongClick = onToggleSelected),
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SelectionCircle(isSelected = isSelected)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "$typeLabel · deleted ${item.deletedAt?.let { ItemFormatting.shortDate(it) } ?: ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun SelectionCircle(isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun BinToolbarAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BinScreenPreview() {
    DigitalListTheme {
        BinContent(
            uiState = BinUiState(
                isLoading = false,
                rows = listOf(
                    BinRow(Item.draft().copy(title = "Old lease — 2023", deletedAt = java.time.Instant.now()), 28),
                    BinRow(Item.draft().copy(title = "Confirm the courier pickup", deletedAt = java.time.Instant.now()), 14),
                ),
                selectedIds = emptySet(),
            ),
            onBack = {},
            onToggleSelected = {},
            onSelectAll = {},
            onClearSelection = {},
            onRestore = {},
            onDeleteForever = {},
        )
    }
}
