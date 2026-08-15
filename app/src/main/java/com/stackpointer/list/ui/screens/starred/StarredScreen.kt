package com.stackpointer.list.ui.screens.starred

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun StarredScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
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
        onCompleteItem = viewModel::completeItem,
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
    onCompleteItem: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Starred") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // TODO(M8): navigate to the search screen once it exists.
                    IconButton(onClick = {}) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    StarredOverflowMenu(expanded = menuExpanded, onDismiss = { menuExpanded = false })
                },
            )
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
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            items(uiState.items, key = { it.id }) { item ->
                ItemRow(
                    title = item.title,
                    metadata = ItemFormatting.metadata(item, now),
                    recurrenceText = item.recurrence?.let { ItemFormatting.recurrenceText(it) },
                    checklistProgress = ItemFormatting.checklistProgress(item),
                    isCompleted = item.isCompleted,
                    isStarred = item.isStarred,
                    onClick = { onOpenItem(item) },
                    onToggleComplete = { onCompleteItem(item.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// Screen 15's global overflow menu. Most rows open screens later milestones build (M8's
// collections/recycle-bin/settings, the sort-by sheet, sync); they're TODO no-ops for now
// rather than invented behaviour.
@Composable
private fun StarredOverflowMenu(expanded: Boolean, onDismiss: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(text = { Text("Sync now") }, leadingIcon = { Icon(Icons.Filled.Sync, null) }, onClick = onDismiss)
        DropdownMenuItem(text = { Text("Select") }, leadingIcon = { Icon(Icons.Filled.CheckBox, null) }, onClick = onDismiss)
        DropdownMenuItem(text = { Text("Sort by") }, leadingIcon = { Icon(Icons.Filled.SwapVert, null) }, onClick = onDismiss)
        DropdownMenuItem(text = { Text("Manage collections") }, leadingIcon = { Icon(Icons.Filled.Style, null) }, onClick = onDismiss)
        DropdownMenuItem(text = { Text("Try these out") }, leadingIcon = { Icon(Icons.Filled.Lightbulb, null) }, onClick = onDismiss)
        HorizontalDivider()
        DropdownMenuItem(text = { Text("Recycle bin") }, leadingIcon = { Icon(Icons.Filled.Delete, null) }, onClick = onDismiss)
        DropdownMenuItem(text = { Text("Settings") }, leadingIcon = { Icon(Icons.Filled.Settings, null) }, onClick = onDismiss)
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
            onCompleteItem = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
