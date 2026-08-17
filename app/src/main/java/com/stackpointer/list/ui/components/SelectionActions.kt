package com.stackpointer.list.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.stackpointer.list.domain.model.Collection

/**
 * Screen 10's selection-mode action row — push_pin / label / archive / more_vert — dropped into
 * [SelectionTopBar]'s `actions` slot. `label` opens a menu of existing collections (screen 23's
 * multi-select-by-collection idea, reused here for bulk tagging); `more_vert` just holds Delete,
 * the one bulk action the design doesn't give its own top-level icon.
 */
@Composable
fun RowScope.SelectionActions(
    collections: List<Collection>,
    onPin: () -> Unit,
    onAddToCollection: (collectionId: String) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
) {
    var labelMenuOpen by remember { mutableStateOf(false) }
    var overflowOpen by remember { mutableStateOf(false) }

    IconButton(onClick = onPin) {
        Icon(imageVector = Icons.Filled.PushPin, contentDescription = "Pin")
    }

    IconButton(onClick = { labelMenuOpen = true }) {
        Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = "Add to collection")
    }
    DropdownMenu(expanded = labelMenuOpen, onDismissRequest = { labelMenuOpen = false }) {
        if (collections.isEmpty()) {
            DropdownMenuItem(text = { Text("No collections yet") }, onClick = {}, enabled = false)
        }
        collections.forEach { collection ->
            DropdownMenuItem(
                text = { Text(collection.name) },
                onClick = {
                    labelMenuOpen = false
                    onAddToCollection(collection.id)
                },
            )
        }
    }

    IconButton(onClick = onArchive) {
        Icon(imageVector = Icons.Filled.Archive, contentDescription = "Archive")
    }

    IconButton(onClick = { overflowOpen = true }) {
        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
        DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            onClick = {
                overflowOpen = false
                onDelete()
            },
        )
    }
}
