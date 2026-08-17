package com.stackpointer.list.ui.screens.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.ui.components.CaptureBar
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.FloatingNavigationBar
import com.stackpointer.list.ui.components.GlobalOverflowMenu
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.NavDestination
import com.stackpointer.list.ui.components.SectionHeader
import com.stackpointer.list.ui.components.SelectionActions
import com.stackpointer.list.ui.components.SelectionTopBar
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.capture.CapturePrefill
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

/**
 * Screen 03 — "Tasks — grouped by when," the FloatingNavigationBar's second destination, which
 * had no screen behind it until now (see git history's TODO on `NavDestination.TASKS`). Two
 * deliberate departures from the screen 03 mock, both because CLAUDE.md's "11–30 wins" rule
 * overrides 01–10 wherever they conflict: a docked [CaptureBar] instead of a solo `add_task`
 * FAB, and the collapsed "Completed · N" footer here navigates to the real Completed screen
 * (08) instead of expanding inline, since that screen already owns completed-item presentation.
 * The row drag handle is visual only, matching [com.stackpointer.list.ui.screens.capture.ChecklistModeContent]'s
 * disclosed limitation — manual reordering isn't wired to a drag gesture yet.
 */
@Composable
fun TasksScreen(
    onOpenItem: (Item) -> Unit,
    onOpenCollections: () -> Unit,
    onOpenCompleted: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectNav: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    TasksContent(
        uiState = uiState,
        onOpenItem = onOpenItem,
        onOpenCollections = onOpenCollections,
        onOpenCompleted = onOpenCompleted,
        onOpenTemplates = onOpenTemplates,
        onOpenRecycleBin = onOpenRecycleBin,
        onOpenSettings = onOpenSettings,
        onSelectNav = onSelectNav,
        onCompleteItem = viewModel::completeItem,
        onOpenCapture = { captureViewModel.openFor(CapturePrefill.NONE) },
        onToggleSelected = viewModel::toggleSelected,
        onClearSelection = viewModel::clearSelection,
        onBulkPin = viewModel::bulkPin,
        onBulkAddToCollection = viewModel::bulkAddToCollection,
        onBulkArchive = viewModel::bulkArchive,
        onBulkDelete = viewModel::bulkDelete,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    CaptureSheet(viewModel = captureViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksContent(
    uiState: TasksUiState,
    onOpenItem: (Item) -> Unit,
    onOpenCollections: () -> Unit,
    onOpenCompleted: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectNav: (NavDestination) -> Unit,
    onCompleteItem: (String) -> Unit,
    onOpenCapture: () -> Unit,
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
                LargeTopAppBar(
                    title = { Text("Tasks") },
                    navigationIcon = {
                        // Decorative — this app has no navigation drawer to open.
                        Icon(Icons.Filled.Menu, contentDescription = null, modifier = Modifier.padding(start = 12.dp))
                    },
                    actions = {
                        IconButton(onClick = {}) { Icon(Icons.Filled.SwapVert, contentDescription = "Sort by") }
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
                    colors = TopAppBarDefaults.topAppBarColors(),
                )
            }
        },
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.isEmpty) {
                EmptyState(
                    headline = "Nothing here yet",
                    supportingText = "Notes, checklists and reminders you create will live on this " +
                        "screen. Start with a thought and sort it out later.",
                    actionLabel = "Write a note",
                    onAction = {},
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val now = Instant.now()
                ExpressivePullToRefreshBox(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 160.dp),
                ) {
                    uiState.groups.forEach { group ->
                        item(key = "header-${group.label}") {
                            SectionHeader(
                                label = whenGroupLabel(group.label),
                                count = group.items.size,
                                isError = group.label == TaskWhenGroup.OVERDUE,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            )
                        }
                        items(group.items, key = { it.id }) { item ->
                            ItemRow(
                                title = item.title,
                                metadata = if (group.label == TaskWhenGroup.OVERDUE) {
                                    ItemFormatting.overdueText(item.dueAt ?: now, now)
                                } else {
                                    ItemFormatting.metadata(item, now)
                                },
                                recurrenceText = item.recurrence?.let { ItemFormatting.recurrenceText(it) },
                                checklistProgress = ItemFormatting.checklistProgress(item),
                                isCompleted = item.isCompleted,
                                isStarred = item.isStarred,
                                isOverdue = group.label == TaskWhenGroup.OVERDUE,
                                onClick = {
                                    if (uiState.isSelectionMode) onToggleSelected(item.id) else onOpenItem(item)
                                },
                                onToggleComplete = { onCompleteItem(item.id) },
                                isSelectionMode = uiState.isSelectionMode,
                                isSelected = item.id in uiState.selectedIds,
                                onLongClick = { onToggleSelected(item.id) },
                        sharedTransitionKey = if (item.isNote) item.id else null,
                                trailingContent = if (uiState.isSelectionMode) null else {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.DragIndicator,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                            )
                        }
                    }
                    item {
                        CompletedFooterRow(count = uiState.completedCount, onClick = onOpenCompleted)
                    }
                }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CaptureBar(placeholder = "Add a task", onClick = onOpenCapture)
                FloatingNavigationBar(selected = NavDestination.TASKS, onSelect = onSelectNav)
            }
        }
    }
}

@Composable
private fun CompletedFooterRow(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (count == 0) return
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Completed · $count",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(imageVector = Icons.Filled.ExpandMore, contentDescription = null)
        }
    }
}

private fun whenGroupLabel(label: TaskWhenGroup): String = when (label) {
    TaskWhenGroup.OVERDUE -> "Overdue"
    TaskWhenGroup.TODAY -> "Today"
    TaskWhenGroup.TOMORROW -> "Tomorrow"
    TaskWhenGroup.LATER -> "Later"
    TaskWhenGroup.NO_DATE -> "No date"
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    DigitalListTheme {
        TasksContent(
            uiState = TasksUiState(isLoading = false),
            onOpenItem = {},
            onOpenCollections = {},
            onOpenCompleted = {},
            onOpenTemplates = {},
            onOpenRecycleBin = {},
            onOpenSettings = {},
            onSelectNav = {},
            onCompleteItem = {},
            onOpenCapture = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
