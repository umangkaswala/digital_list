package com.stackpointer.list.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.ui.components.CaptureBar
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.FloatingNavigationBar
import com.stackpointer.list.ui.components.GlobalOverflowMenu
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.NavDestination
import com.stackpointer.list.ui.components.SectionHeader
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.components.ViewTile
import com.stackpointer.list.ui.components.showUndoSnackbar
import com.stackpointer.list.ui.screens.capture.CapturePrefill
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun HomeScreen(
    onOpenToday: () -> Unit,
    onOpenScheduled: () -> Unit,
    onOpenStarred: () -> Unit,
    onOpenPlace: () -> Unit,
    onOpenNoAlert: () -> Unit,
    onOpenCompleted: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (snackbarHostState.showUndoSnackbar(event.message)) viewModel.undo(event)
        }
    }

    HomeContent(
        uiState = uiState,
        onOpenToday = onOpenToday,
        onOpenScheduled = onOpenScheduled,
        onOpenStarred = onOpenStarred,
        onOpenPlace = onOpenPlace,
        onOpenNoAlert = onOpenNoAlert,
        onOpenCompleted = onOpenCompleted,
        onOpenItem = onOpenItem,
        onOpenSearch = onOpenSearch,
        onOpenCollections = onOpenCollections,
        onOpenTemplates = onOpenTemplates,
        onOpenRecycleBin = onOpenRecycleBin,
        onOpenSettings = onOpenSettings,
        onCompleteItem = viewModel::completeItem,
        onOpenCapture = { captureViewModel.openFor(CapturePrefill.NONE) },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    CaptureSheet(viewModel = captureViewModel)
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onOpenToday: () -> Unit,
    onOpenScheduled: () -> Unit,
    onOpenStarred: () -> Unit,
    onOpenPlace: () -> Unit,
    onOpenNoAlert: () -> Unit,
    onOpenCompleted: () -> Unit,
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
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = { UndoSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val now = Instant.now()

            ExpressivePullToRefreshBox(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 160.dp),
            ) {
                item {
                    DockedSearchBar(
                        onOpenSearch = onOpenSearch,
                        onOpenCollections = onOpenCollections,
                        onOpenTemplates = onOpenTemplates,
                        onOpenRecycleBin = onOpenRecycleBin,
                        onOpenSettings = onOpenSettings,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                item {
                    TileGrid(
                        uiState = uiState,
                        onOpenToday = onOpenToday,
                        onOpenScheduled = onOpenScheduled,
                        onOpenStarred = onOpenStarred,
                        onOpenPlace = onOpenPlace,
                        onOpenNoAlert = onOpenNoAlert,
                        onOpenCompleted = onOpenCompleted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                uiState.buckets.forEach { bucket ->
                    item(key = "header-${bucket.label}") {
                        SectionHeader(
                            label = bucketLabelText(bucket.label),
                            count = bucket.items.size,
                            isError = bucket.label == BucketLabel.PAST,
                            modifier = Modifier.padding(horizontal = 16.dp),
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
                            onClick = { onOpenItem(item) },
                            onToggleComplete = { onCompleteItem(item.id) },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                        )
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
                CaptureBar(
                    placeholder = "Add a note or task",
                    onClick = onOpenCapture,
                )
                FloatingNavigationBar(
                    selected = NavDestination.HOME,
                    onSelect = { destination ->
                        when (destination) {
                            NavDestination.HOME -> Unit
                            // TODO: no Tasks screen in CLAUDE.md's target ui/screens list — stays unwired.
                            NavDestination.TASKS -> Unit
                            NavDestination.COLLECTIONS -> onOpenCollections()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TileGrid(
    uiState: HomeUiState,
    onOpenToday: () -> Unit,
    onOpenScheduled: () -> Unit,
    onOpenStarred: () -> Unit,
    onOpenPlace: () -> Unit,
    onOpenNoAlert: () -> Unit,
    onOpenCompleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewTile(
                icon = Icons.Filled.Today,
                label = "Today",
                count = "${uiState.todayDone}/${uiState.todayTotal}",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = onOpenToday,
                modifier = Modifier.weight(1f),
            )
            ViewTile(
                icon = Icons.Filled.Schedule,
                label = "Scheduled",
                count = uiState.scheduledCount.toString(),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onOpenScheduled,
                modifier = Modifier.weight(1f),
            )
            ViewTile(
                icon = Icons.Filled.Star,
                label = "Starred",
                count = uiState.starredCount.toString(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = onOpenStarred,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewTile(
                icon = Icons.Filled.LocationOn,
                label = "Place",
                count = uiState.placeCount.toString(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onOpenPlace,
                modifier = Modifier.weight(1f),
            )
            ViewTile(
                icon = Icons.Filled.NotificationsOff,
                label = "No alert",
                count = uiState.noAlertCount.toString(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onOpenNoAlert,
                modifier = Modifier.weight(1f),
            )
            ViewTile(
                icon = Icons.Filled.DoneAll,
                label = "Completed",
                count = uiState.completedCount.toString(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = onOpenCompleted,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DockedSearchBar(
    onOpenSearch: () -> Unit,
    onOpenCollections: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenRecycleBin: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overflowOpen by remember { mutableStateOf(false) }

    Surface(
        onClick = onOpenSearch,
        // A minimum, not a fixed height — at large font scales the row needs more vertical
        // room than 56dp even with a single-line, ellipsized placeholder.
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Search notes and tasks",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            Box {
                IconButton(onClick = { overflowOpen = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options")
                }
                GlobalOverflowMenu(
                    expanded = overflowOpen,
                    onDismiss = { overflowOpen = false },
                    onManageCollections = onOpenCollections,
                    onTryTheseOut = onOpenTemplates,
                    onRecycleBin = onOpenRecycleBin,
                    onSettings = onOpenSettings,
                )
            }
        }
    }
}

private fun bucketLabelText(label: BucketLabel): String = when (label) {
    BucketLabel.PAST -> "Past"
    BucketLabel.TODAY -> "Today"
    BucketLabel.SOON -> "Soon"
    BucketLabel.NEXT_7_DAYS -> "Next 7 days"
    BucketLabel.LATER -> "Later"
    BucketLabel.EARLIER_THIS_WEEK -> "Earlier this week"
    BucketLabel.OLDER -> "Older"
    BucketLabel.COMPLETED -> "Completed"
    BucketLabel.UNBUCKETED -> ""
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DigitalListTheme {
        HomeContent(
            uiState = HomeUiState(
                isLoading = false,
                todayDone = 1,
                todayTotal = 5,
                scheduledCount = 5,
                starredCount = 2,
                placeCount = 1,
                noAlertCount = 7,
                completedCount = 308,
            ),
            onOpenToday = {},
            onOpenScheduled = {},
            onOpenStarred = {},
            onOpenPlace = {},
            onOpenNoAlert = {},
            onOpenCompleted = {},
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
