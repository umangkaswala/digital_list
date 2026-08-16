package com.stackpointer.list.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.stackpointer.list.Features

/**
 * Screen 15's "global overflow menu" — anchored from every list screen's top bar `more_vert`.
 * `Sync now` (account sync), `Select` (bulk selection) and `Sort by` (per-view sort order) are
 * shown per the design but disabled: none of the list screens have selection mode or a
 * sort-order-aware query built yet (only the recycle bin got a scoped selection mode, for M8's
 * own screen), and account sync stays behind [Features.accountSync] like the rest of the app.
 */
@Composable
fun GlobalOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onManageCollections: () -> Unit,
    onTryTheseOut: () -> Unit,
    onRecycleBin: () -> Unit,
    onSettings: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Sync now") },
            leadingIcon = { Icon(Icons.Filled.Sync, contentDescription = null) },
            onClick = onDismiss,
            enabled = Features.accountSync,
        )
        DropdownMenuItem(
            text = { Text("Select") },
            leadingIcon = { Icon(Icons.Filled.CheckBox, contentDescription = null) },
            onClick = onDismiss,
            enabled = false,
        )
        DropdownMenuItem(
            text = { Text("Sort by") },
            leadingIcon = { Icon(Icons.Filled.SwapVert, contentDescription = null) },
            onClick = onDismiss,
            enabled = false,
        )
        DropdownMenuItem(
            text = { Text("Manage collections") },
            leadingIcon = { Icon(Icons.Filled.Style, contentDescription = null) },
            onClick = { onDismiss(); onManageCollections() },
        )
        DropdownMenuItem(
            text = { Text("Try these out") },
            leadingIcon = { Icon(Icons.Filled.Lightbulb, contentDescription = null) },
            onClick = { onDismiss(); onTryTheseOut() },
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Recycle bin") },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = { onDismiss(); onRecycleBin() },
        )
        DropdownMenuItem(
            text = { Text("Settings") },
            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            onClick = { onDismiss(); onSettings() },
        )
    }
}
