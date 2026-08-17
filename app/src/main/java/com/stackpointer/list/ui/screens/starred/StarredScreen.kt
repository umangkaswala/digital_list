package com.stackpointer.list.ui.screens.starred

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.GlobalOverflowMenu
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.SelectionActions
import com.stackpointer.list.ui.components.SelectionTopBar
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun StarredScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StarredViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    StarredContent(
        uiState = uiState,
        onBack = onBack,
        onOpenItem = onOpenItem,
        onOpenSearch = onOpenSearch,
        onOpenCollections = onOpenCollections,
        onOpenTemplates = onOpenTemplates,
        onOpenRecycleBin = onOpenRecycleBin,
        onOpenSettings = onOpenSettings,
        onCompleteItem = viewModel::completeItem,
        onToggleSelected = viewModel::toggleSelected,
        onClearSelection = viewModel::clearSelection,
        onBulkPin = viewModel::bulkPin,
        onBulkAddToCollection = viewModel::bulkAddToCollection,
        onBulkArchive = viewModel::bulkArchive,
        onBulkDelete = viewModel::bulkDelete,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StarredContent(
    uiState: StarredUiState,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    onCompleteItem: (String) -> Unit,
    onToggleSelected: (String) -> Unit = {},
    onClearSelection: () -> Unit = {},
    onBulkPin: () -> Unit = {},
    onBulkAddToCollection: (String) -> Unit = {},
    onBulkArchive: () -> Unit = {},
    onBulkDelete: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedIds.size,
                    onClose = onClearSelection,
                    actions = {
                        SelectionActions(
                            collections = uiState.collections,
                            onPin = onBulkPin,
                            onAddToCollection = onBulkAddToCollection,
                            onArchive = onBulkArchive,
                            onDelete = onBulkDelete,
                        )
                    },
                )
            } else {
                TopAppBar(
                    title = { Text("Starred") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onOpenSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        GlobalOverflowMenu(
                            expanded = menuExpanded,
                            onDismiss = { menuExpanded = false },
                            onManageCollections = onOpenCollections,
                            onTryTheseOut = onOpenTemplates,
                            onRecycleBin = onOpenRecycleBin,
                            onSettings = onOpenSettings,
                        )
                    },
                )
            }
        },
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState.isEmpty) {
            EmptyState(
                headline = "Nothing here yet",
                supportingText = "Notes, checklists and reminders you create will live on this " +
                    "screen. Start with a thought and sort it out later.",
                actionLabel = "Write a note",
                onAction = {},
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            return@Scaffold
        }

        val now = Instant.now()
        ExpressivePullToRefreshBox(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.items, key = { it.id }) { item ->
                ItemRow(
                    title = item.title,
                    metadata = ItemFormatting.metadata(item, now),
                    recurrenceText = item.recurrence?.let { ItemFormatting.recurrenceText(it) },
                    checklistProgress = ItemFormatting.checklistProgress(item),
                    isCompleted = item.isCompleted,
                    isStarred = item.isStarred,
                    onClick = {
                        if (uiState.isSelectionMode) onToggleSelected(item.id) else onOpenItem(item)
                    },
                    onToggleComplete = { onCompleteItem(item.id) },
                    isSelectionMode = uiState.isSelectionMode,
                    isSelected = item.id in uiState.selectedIds,
                    onLongClick = { onToggleSelected(item.id) },
                    sharedTransitionKey = if (item.isNote) item.id else null,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                )
            }
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StarredScreenPreview() {
    DigitalListTheme {
        StarredContent(
            uiState = StarredUiState(isLoading = false),
            onBack = {},
            onOpenItem = {},
            onOpenSearch = {},
            onOpenCollections = {},
            onOpenTemplates = {},
            onOpenRecycleBin = {},
            onOpenSettings = {},
            onCompleteItem = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
