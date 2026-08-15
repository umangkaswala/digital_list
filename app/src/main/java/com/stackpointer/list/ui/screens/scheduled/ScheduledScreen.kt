package com.stackpointer.list.ui.screens.scheduled

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.ui.components.CaptureBar
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.SectionHeader
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun ScheduledScreen(
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduledViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    ScheduledContent(
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
private fun ScheduledContent(
    uiState: ScheduledUiState,
    onBack: () -> Unit,
    onOpenItem: (String) -> Unit,
    onCompleteItem: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Scheduled") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // TODO(M8): wire the sort-by sheet (screen 26) once it exists.
                    IconButton(onClick = {}) { Icon(Icons.Filled.SwapVert, contentDescription = "Sort by") }
                    IconButton(onClick = {}) { Icon(Icons.Filled.MoreVert, contentDescription = "More options") }
                },
            )
        },
        bottomBar = {
            CaptureBar(
                placeholder = "Add a scheduled task",
                // TODO(M5): open the capture sheet in time mode.
                onClick = {},
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
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            uiState.buckets.forEach { bucket ->
                item(key = "header-${bucket.label}") {
                    SectionHeader(
                        label = scheduledBucketLabel(bucket.label),
                        count = bucket.items.size,
                        isError = bucket.label == BucketLabel.PAST,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
                items(bucket.items, key = { it.id }) { item ->
                    ItemRow(
                        title = item.title,
                        metadata = if (bucket.label == BucketLabel.PAST) {
                            ItemFormatting.overdueText(item.dueAt ?: now, now)
                        } else {
                            ItemFormatting.metadata(item, now)
                        },
                        recurrenceText = item.recurrence?.let { ItemFormatting.recurrenceText(it) },
                        checklistProgress = ItemFormatting.checklistProgress(item),
                        isCompleted = item.isCompleted,
                        isStarred = item.isStarred,
                        isOverdue = bucket.label == BucketLabel.PAST,
                        onClick = { onOpenItem(item.id) },
                        onToggleComplete = { onCompleteItem(item.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
            }
            item { androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 88.dp)) }
        }
    }
}

private fun scheduledBucketLabel(label: BucketLabel): String = when (label) {
    BucketLabel.PAST -> "Past"
    BucketLabel.TODAY -> "Today"
    BucketLabel.NEXT_7_DAYS -> "Next 7 days"
    BucketLabel.LATER -> "Later"
    else -> ""
}

@Preview(showBackground = true)
@Composable
private fun ScheduledScreenPreview() {
    DigitalListTheme {
        ScheduledContent(
            uiState = ScheduledUiState(isLoading = false),
            onBack = {},
            onOpenItem = {},
            onCompleteItem = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
