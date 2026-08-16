package com.stackpointer.list.ui.screens.completed

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.BucketLabel
import com.stackpointer.list.ui.components.EmptyState
import com.stackpointer.list.ui.components.ExpressivePullToRefreshBox
import com.stackpointer.list.ui.components.ItemRow
import com.stackpointer.list.ui.components.SectionHeader
import com.stackpointer.list.ui.components.UndoSnackbarHost
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

@Composable
fun CompletedScreen(
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CompletedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CompletedContent(
        uiState = uiState,
        onBack = onBack,
        onOpenItem = onOpenItem,
        onRestore = viewModel::restoreToIncomplete,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletedContent(
    uiState: CompletedUiState,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit,
    onRestore: (Item) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Completed") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // TODO(M8): a real bulk-clear needs a confirmation dialog and a
                    // multi-item undo the current single-token model doesn't support yet.
                    TextButton(onClick = {}) { Text("Clear all") }
                },
            )
        },
        snackbarHost = { UndoSnackbarHost(remember { SnackbarHostState() }) },
    ) { innerPadding ->
        if (uiState.isEmpty) {
            EmptyState(
                headline = "Nothing here yet",
                supportingText = "Items you complete will show up here.",
                actionLabel = "Write a note",
                onAction = {},
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            return@Scaffold
        }

        ExpressivePullToRefreshBox(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            uiState.buckets.forEach { bucket ->
                item(key = "header-${bucket.label}") {
                    SectionHeader(
                        label = completedBucketLabel(bucket.label).uppercase(),
                        count = bucket.items.size,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    )
                }
                items(bucket.items, key = { it.id }) { item ->
                    ItemRow(
                        title = item.title,
                        metadata = item.completedAt?.let { ItemFormatting.completedText(it) }
                            ?.let { done -> ItemFormatting.collectionNames(item)?.let { "$done · $it" } ?: done },
                        isCompleted = true,
                        isStarred = item.isStarred,
                        onClick = { onOpenItem(item) },
                        onToggleComplete = { onRestore(item) },
                        trailingContent = {
                            IconButton(onClick = { onRestore(item) }) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Restore ${item.title}")
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .animateItem(placementSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                    )
                }
            }
        }
        }
    }
}

private fun completedBucketLabel(label: BucketLabel): String = when (label) {
    BucketLabel.TODAY -> "Today"
    BucketLabel.EARLIER_THIS_WEEK -> "Earlier this week"
    BucketLabel.OLDER -> "Older"
    else -> ""
}

@Preview(showBackground = true)
@Composable
private fun CompletedScreenPreview() {
    DigitalListTheme {
        CompletedContent(uiState = CompletedUiState(isLoading = false), onBack = {}, onOpenItem = {}, onRestore = {})
    }
}
