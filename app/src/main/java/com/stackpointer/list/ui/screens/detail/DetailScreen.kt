package com.stackpointer.list.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.TriggerType
import com.stackpointer.list.ui.components.NotificationBarMenuItems
import com.stackpointer.list.ui.screens.capture.CaptureMode
import com.stackpointer.list.ui.screens.capture.CaptureSheet
import com.stackpointer.list.ui.screens.capture.CaptureViewModel
import com.stackpointer.list.ui.screens.common.ItemFormatting
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant

/** Screen 24 — tasks/reminders (items without a note body; see [Item.isNote]). Attribute rows
 * reuse the capture sheet's own pickers via [CaptureViewModel.openForExisting] rather than
 * duplicating date/repeat/alert UI a second time. */
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = uiState.item

    if (item != null) {
        DetailContent(
            item = item,
            onBack = onBack,
            onToggleStarred = viewModel::toggleStarred,
            onComplete = { viewModel.complete(); onBack() },
            onDelete = { viewModel.delete(); onBack() },
            onEditFull = { captureViewModel.openForExisting(item, CaptureMode.NONE) },
            onEditTime = { captureViewModel.openForExisting(item, CaptureMode.TIME) },
            onEditRepeat = {
                captureViewModel.openForExisting(item, CaptureMode.TIME)
                captureViewModel.openRepeatPicker()
            },
            onEditEarlyAlert = {
                captureViewModel.openForExisting(item, CaptureMode.TIME)
                captureViewModel.openEarlyAlertMenu()
            },
            onEditAlertType = {
                captureViewModel.openForExisting(item, CaptureMode.TIME)
                captureViewModel.openAlertTypeSheet()
            },
            onEditCollections = { captureViewModel.openForExisting(item, CaptureMode.LABEL) },
            onToggleShownInNotificationBar = viewModel::toggleShownInNotificationBar,
            onTogglePinnedToNotification = viewModel::togglePinnedToNotification,
            modifier = modifier,
        )
    }
    CaptureSheet(viewModel = captureViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    item: Item,
    onBack: () -> Unit,
    onToggleStarred: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onEditFull: () -> Unit,
    onEditTime: () -> Unit,
    onEditRepeat: () -> Unit,
    onEditEarlyAlert: () -> Unit,
    onEditAlertType: () -> Unit,
    onEditCollections: () -> Unit,
    onToggleShownInNotificationBar: () -> Unit,
    onTogglePinnedToNotification: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var overflowMenuOpen by remember { mutableStateOf(false) }

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
                    IconButton(onClick = onToggleStarred) {
                        Icon(
                            imageVector = if (item.isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (item.isStarred) "Unstar" else "Star",
                        )
                    }
                    IconButton(onClick = { overflowMenuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = overflowMenuOpen, onDismissRequest = { overflowMenuOpen = false }) {
                        NotificationBarMenuItems(
                            isShownInNotificationBar = item.isShownInNotificationBar,
                            isPinnedToNotification = item.isPinnedToNotification,
                            onToggleShown = { overflowMenuOpen = false; onToggleShownInNotificationBar() },
                            onTogglePinned = { overflowMenuOpen = false; onTogglePinnedToNotification() },
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = 96.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )

                val now = Instant.now()
                item.dueAt?.let {
                    AttributeRow(Icons.Filled.Event, ItemFormatting.dueText(it, now), onEditTime)
                }
                if (item.dueAt != null) {
                    AttributeRow(
                        icon = Icons.Filled.Repeat,
                        text = item.recurrence?.let { ItemFormatting.recurrenceText(it) } ?: "Never",
                        onClick = onEditRepeat,
                    )
                    AttributeRow(
                        icon = Icons.Filled.NotificationsActive,
                        text = earlyAlertLabel(item.earlyAlertMinutes),
                        onClick = onEditEarlyAlert,
                    )
                    AttributeRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        text = item.alertType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = onEditAlertType,
                    )
                }
                item.collections.firstOrNull()?.let {
                    AttributeRow(Icons.AutoMirrored.Filled.Label, it.name, onEditCollections)
                }

                Text(
                    text = "Last modified ${ItemFormatting.fullDate(item.updatedAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    ToolbarAction(Icons.Filled.Check, "Complete", onComplete)
                    ToolbarAction(Icons.Filled.Edit, "Edit", onEditFull)
                    ToolbarAction(Icons.Filled.Share, "Share", {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, item.body?.let { "${item.title}\n\n$it" } ?: item.title)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    })
                    ToolbarAction(Icons.Filled.Delete, "Delete", onDelete)
                }
            }
        }
    }
}

@Composable
private fun AttributeRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ToolbarAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun earlyAlertLabel(minutes: Int?): String = when (minutes) {
    null -> "No early alert"
    10 -> "10 minutes before"
    15 -> "15 minutes before"
    60 -> "1 hour before"
    1440 -> "1 day before"
    else -> "$minutes minutes before"
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    DigitalListTheme {
        DetailContent(
            item = Item.draft().copy(
                title = "Call mum",
                triggerType = TriggerType.TIME,
                dueAt = Instant.now(),
                alertType = AlertType.MEDIUM,
                earlyAlertMinutes = 10,
            ),
            onBack = {},
            onToggleStarred = {},
            onComplete = {},
            onDelete = {},
            onEditFull = {},
            onEditTime = {},
            onEditRepeat = {},
            onEditEarlyAlert = {},
            onEditAlertType = {},
            onEditCollections = {},
            onToggleShownInNotificationBar = {},
            onTogglePinnedToNotification = {},
        )
    }
}
