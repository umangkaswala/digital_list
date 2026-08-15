package com.stackpointer.list.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * The two notification-bar toggle rows shared by the detail (24) and editor (04) overflow
 * menus and the capture sheet's own overflow — labelled rows, not bare icon buttons, per
 * CLAUDE.md's notification-bar & pin feature spec.
 *
 * The spec calls for Material Symbols `keep`/`keep_off` for "pin to notification" — deliberately
 * distinct from `push_pin`, already used for the unrelated in-app pin-to-top-of-list feature.
 * Neither glyph exists in this project's icon set: `androidx.compose.material:material-icons-extended`
 * mirrors an older, frozen Material Icons snapshot that predates Google Keep's `keep`/`keep_off`
 * symbols, and adding a second icon library would violate CLAUDE.md's "no other UI or design
 * libraries" rule. [Icons.Filled.BookmarkAdded]/[Icons.Filled.BookmarkBorder] is the closest
 * available substitute that still reads as "kept/persistent" without colliding visually with
 * `push_pin` — flagged here rather than silently reusing `push_pin` or inventing a meaning for
 * an unrelated glyph.
 */
@Composable
fun NotificationBarMenuItems(
    isShownInNotificationBar: Boolean,
    isPinnedToNotification: Boolean,
    onToggleShown: () -> Unit,
    onTogglePinned: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(if (isPinnedToNotification) "Unpin from notification" else "Pin to notification") },
        leadingIcon = {
            Icon(
                imageVector = if (isPinnedToNotification) Icons.Filled.BookmarkAdded else Icons.Filled.BookmarkBorder,
                contentDescription = null,
            )
        },
        onClick = onTogglePinned,
    )
    DropdownMenuItem(
        text = { Text(if (isShownInNotificationBar) "Hide from notification bar" else "Show in notification bar") },
        leadingIcon = {
            Icon(
                imageVector = if (isShownInNotificationBar) Icons.Filled.Notifications else Icons.Filled.NotificationsNone,
                contentDescription = null,
            )
        },
        onClick = onToggleShown,
    )
}
