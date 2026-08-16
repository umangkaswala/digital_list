package com.stackpointer.list.ui.screens.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stackpointer.list.Features
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SubItem
import com.stackpointer.list.ui.components.NotificationBarMenuItems
import com.stackpointer.list.ui.theme.DigitalListTheme
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Screen 04 — notes (items with a body; see [com.stackpointer.list.domain.model.Item.isNote]).
 * Autosaves on every edit (debounced) — there's no Save button, only the status line.
 * `notifications_none` in the top bar is shown for design fidelity but left a no-op: the
 * handoff never documents what it's for and it isn't the notification-bar feature (that uses
 * its own `notifications`/`keep` icons per CLAUDE.md), so inventing a meaning for it here
 * would contradict "ask rather than invent."
 */
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) return

    EditorContent(
        uiState = uiState,
        onBack = onBack,
        onTitleChange = viewModel::updateTitle,
        onBodyChange = viewModel::updateBody,
        onTogglePinned = viewModel::togglePinned,
        onToggleShownInNotificationBar = viewModel::toggleShownInNotificationBar,
        onTogglePinnedToNotification = viewModel::togglePinnedToNotification,
        onToggleCollection = viewModel::toggleCollection,
        onAddChecklistItem = viewModel::addChecklistItem,
        onChecklistItemTextChange = viewModel::updateChecklistItemText,
        onToggleChecklistItem = viewModel::toggleChecklistItem,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorContent(
    uiState: EditorUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTogglePinned: () -> Unit,
    onToggleShownInNotificationBar: () -> Unit,
    onTogglePinnedToNotification: () -> Unit,
    onToggleCollection: (Collection) -> Unit,
    onAddChecklistItem: () -> Unit,
    onChecklistItemTextChange: (String, String) -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var collectionPickerOpen by remember { mutableStateOf(false) }
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
                    IconButton(onClick = onTogglePinned) {
                        Icon(
                            imageVector = if (uiState.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (uiState.isPinned) "Unpin" else "Pin to top",
                        )
                    }
                    IconButton(onClick = {}) {
                        // Undocumented in the handoff and left inert (see this file's class
                        // doc) — but a TalkBack user still lands on this control, so it gets a
                        // literal, non-inventive label describing what's visually shown rather
                        // than staying unlabeled.
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { overflowMenuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = overflowMenuOpen, onDismissRequest = { overflowMenuOpen = false }) {
                        NotificationBarMenuItems(
                            isShownInNotificationBar = uiState.isShownInNotificationBar,
                            isPinnedToNotification = uiState.isPinnedToNotification,
                            onToggleShown = { overflowMenuOpen = false; onToggleShownInNotificationBar() },
                            onTogglePinned = { overflowMenuOpen = false; onTogglePinnedToNotification() },
                        )
                    }
                },
            )
        },
        bottomBar = { EditorToolbar(onAddChecklistItem = onAddChecklistItem) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            TextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                textStyle = MaterialTheme.typography.headlineMedium,
                colors = plainTextFieldColors(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(uiState.collections, key = { it.id }) { collection ->
                    FilterChip(selected = true, onClick = { onToggleCollection(collection) }, label = { Text(collection.name) })
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { collectionPickerOpen = true },
                        label = { Text("Label") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.height(18.dp)) },
                    )
                }
            }

            TextField(
                value = uiState.body,
                onValueChange = onBodyChange,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = plainTextFieldColors(),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            )

            uiState.subItems.forEach { subItem ->
                ChecklistRow(subItem = subItem, onTextChange = onChecklistItemTextChange, onToggle = onToggleChecklistItem)
            }

            Text(
                text = savedStatusText(uiState.lastSavedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    if (collectionPickerOpen) {
        CollectionPickerDialog(
            allCollections = uiState.allCollections,
            selected = uiState.collections,
            onToggle = onToggleCollection,
            onDismiss = { collectionPickerOpen = false },
        )
    }
}

@Composable
private fun ChecklistRow(subItem: SubItem, onTextChange: (String, String) -> Unit, onToggle: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = subItem.isCompleted, onCheckedChange = { onToggle(subItem.id) })
        TextField(
            value = subItem.text,
            onValueChange = { onTextChange(subItem.id, it) },
            colors = plainTextFieldColors(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EditorToolbar(onAddChecklistItem: () -> Unit) {
    androidx.compose.material3.Surface(shadowElevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Bold/italic/palette need rich-text infrastructure this app doesn't have — the
            // body is a plain String end to end, so there's no span model to format yet.
            IconButton(onClick = {}) { Icon(Icons.Filled.FormatBold, contentDescription = "Bold") }
            IconButton(onClick = {}) { Icon(Icons.Filled.FormatItalic, contentDescription = "Italic") }
            IconButton(onClick = onAddChecklistItem) { Icon(Icons.Filled.Checklist, contentDescription = "Add checklist item") }
            IconButton(onClick = { /* Features.imageAttachments is off */ }) {
                Icon(Icons.Filled.Image, contentDescription = "Image")
            }
            IconButton(onClick = { if (Features.voiceCapture) Unit }) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice")
            }
            IconButton(onClick = {}) { Icon(Icons.Filled.Palette, contentDescription = "Highlight") }
        }
    }
}

@Composable
private fun CollectionPickerDialog(
    allCollections: List<Collection>,
    selected: List<Collection>,
    onToggle: (Collection) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Label") },
        text = {
            Column {
                allCollections.forEach { collection ->
                    val checked = selected.any { it.id == collection.id }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { onToggle(collection) })
                        Text(collection.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun plainTextFieldColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
)

private fun savedStatusText(lastSavedAt: Instant?): String {
    if (lastSavedAt == null) return ""
    val secondsAgo = ChronoUnit.SECONDS.between(lastSavedAt, Instant.now())
    return if (secondsAgo < 60) "Saved just now" else "Saved ${secondsAgo / 60} min ago"
}

@Preview(showBackground = true)
@Composable
private fun EditorScreenPreview() {
    DigitalListTheme {
        EditorContent(
            uiState = EditorUiState(
                isLoading = false,
                originalItem = Item.draft(),
                title = "Standup notes — 14 Aug",
                body = "Friday 10:30 design review. Ship the onboarding copy by Wednesday.",
                lastSavedAt = Instant.now(),
            ),
            onBack = {},
            onTitleChange = {},
            onBodyChange = {},
            onTogglePinned = {},
            onToggleShownInNotificationBar = {},
            onTogglePinnedToNotification = {},
            onToggleCollection = {},
            onAddChecklistItem = {},
            onChecklistItemTextChange = { _, _ -> },
            onToggleChecklistItem = {},
        )
    }
}
