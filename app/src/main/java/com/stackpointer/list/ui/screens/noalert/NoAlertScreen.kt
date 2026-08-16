package com.stackpointer.list.ui.screens.noalert

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.stackpointer.list.domain.model.Template
import com.stackpointer.list.domain.model.TemplateDraft
import com.stackpointer.list.ui.components.CaptureBar
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.GlobalOverflowMenu
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.capture.CaptureMode
import com.stackpointer.list.ui.screens.capture.CapturePrefill
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun NoAlertScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoAlertViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    NoAlertContent(
        uiState = uiState,
        onBack = onBack,
        onOpenItem = onOpenItem,
        onOpenSearch = onOpenSearch,
        onOpenCollections = onOpenCollections,
        onOpenTemplates = onOpenTemplates,
        onOpenRecycleBin = onOpenRecycleBin,
        onOpenSettings = onOpenSettings,
        onOpenCapture = { captureViewModel.openFor(CapturePrefill.NONE) },
        onAddTime = { item -> captureViewModel.openForExisting(item, CaptureMode.TIME) },
        onCommitTemplate = viewModel::commitTemplate,
        onCompleteItem = viewModel::completeItem,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    CaptureSheet(viewModel = captureViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoAlertContent(
    uiState: NoAlertUiState,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCapture: () -> Unit,
    onAddTime: (Item) -> Unit,
    onCommitTemplate: (TemplateDraft) -> Unit,
    onCompleteItem: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("No alert") },
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
        },
        bottomBar = {
            CaptureBar(
                placeholder = "Add a note or task",
                onClick = onOpenCapture,
                modifier = Modifier.padding(16.dp),
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
        ExpressivePullToRefreshBox(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "Nothing here will alert you. Add a time or a place when you want one to.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
            items(uiState.items, key = { it.id }) { item ->
                ItemRow(
                    title = item.title,
                    metadata = ItemFormatting.metadata(item, now),
                    checklistProgress = ItemFormatting.checklistProgress(item),
                    isCompleted = item.isCompleted,
                    isStarred = item.isStarred,
                    onClick = { onOpenItem(item) },
                    onToggleComplete = { onCompleteItem(item.id) },
                    trailingContent = {
                        AssistChip(
                            onClick = { onAddTime(item) },
                            label = { Text("Add time") },
                            leadingIcon = { Icon(Icons.Filled.AlarmAdd, contentDescription = null, modifier = Modifier.padding(0.dp)) },
                            colors = AssistChipDefaults.assistChipColors(),
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                )
            }
            uiState.previewTemplate?.let { template ->
                item {
                    TryTheseOutFooter(
                        template = template,
                        onCommit = { onCommitTemplate(template.draft) },
                        onOpenTemplates = onOpenTemplates,
                    )
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 88.dp)) }
        }
        }
    }
}

@Composable
private fun TryTheseOutFooter(template: Template, onCommit: () -> Unit, onOpenTemplates: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onOpenTemplates,
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = "Try these out", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCommit) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add ${template.title}")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoAlertScreenPreview() {
    DigitalListTheme {
        NoAlertContent(
            uiState = NoAlertUiState(isLoading = false),
            onBack = {},
            onOpenItem = {},
            onOpenSearch = {},
            onOpenCollections = {},
            onOpenTemplates = {},
            onOpenRecycleBin = {},
            onOpenSettings = {},
            onOpenCapture = {},
            onAddTime = {},
            onCommitTemplate = {},
            onCompleteItem = {},
        )
    }
}
