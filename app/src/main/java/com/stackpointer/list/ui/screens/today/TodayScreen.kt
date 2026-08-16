package com.stackpointer.list.ui.screens.today

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.ui.components.CaptureBar
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.GlobalOverflowMenu
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.SectionHeader
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.capture.CapturePrefill
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun TodayScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    TodayContent(
        uiState = uiState,
        onBack = onBack,
        onOpenItem = onOpenItem,
        onOpenSearch = onOpenSearch,
        onOpenCollections = onOpenCollections,
        onOpenTemplates = onOpenTemplates,
        onOpenRecycleBin = onOpenRecycleBin,
        onOpenSettings = onOpenSettings,
        onCompleteItem = viewModel::completeItem,
        onOpenCapture = { captureViewModel.openFor(CapturePrefill.TODAY) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    CaptureSheet(viewModel = captureViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayContent(
    uiState: TodayUiState,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    onCompleteItem: (String) -> Unit,
    onOpenCapture: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
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
                placeholder = "Add to today",
                onClick = onOpenCapture,
                modifier = Modifier.padding(16.dp),
            )
        },
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (uiState.totalCount == 0 && !uiState.isLoading) {
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
                    text = "Today",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                )
                Text(
                    text = "${uiState.doneCount} of ${uiState.totalCount} done",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                )
            }
            uiState.buckets.forEach { bucket ->
                item(key = "header-${bucket.label}") {
                    SectionHeader(
                        label = todayBucketLabel(bucket.label),
                        count = bucket.items.size,
                        isError = bucket.label == BucketLabel.PAST,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
                items(bucket.items, key = { it.id }) { item ->
                    ItemRow(
                        title = item.title,
                        metadata = if (bucket.label == BucketLabel.COMPLETED) {
                            item.completedAt?.let { ItemFormatting.completedText(it) }
                        } else if (bucket.label == BucketLabel.PAST) {
                            ItemFormatting.overdueText(item.dueAt ?: now, now)
                        } else {
                            ItemFormatting.metadata(item, now)
                        },
                        recurrenceText = item.recurrence?.let { ItemFormatting.recurrenceText(it) },
                        checklistProgress = ItemFormatting.checklistProgress(item),
                        isCompleted = item.isCompleted,
                        isStarred = item.isStarred,
                        isOverdue = bucket.label == BucketLabel.PAST,
                        onClick = { onOpenItem(item) },
                        onToggleComplete = { onCompleteItem(item.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                    )
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 88.dp)) }
        }
        }
    }
}

private fun todayBucketLabel(label: BucketLabel): String = when (label) {
    BucketLabel.PAST -> "Past"
    BucketLabel.SOON -> "Soon"
    BucketLabel.COMPLETED -> "Completed"
    else -> ""
}

@Preview(showBackground = true)
@Composable
private fun TodayScreenPreview() {
    DigitalListTheme {
        TodayContent(
            uiState = TodayUiState(isLoading = false, doneCount = 1, totalCount = 5),
            onBack = {},
            onOpenItem = {},
            onOpenSearch = {},
            onOpenCollections = {},
            onOpenTemplates = {},
            onOpenRecycleBin = {},
            onOpenSettings = {},
            onCompleteItem = {},
            onOpenCapture = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
