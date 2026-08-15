package com.stackpointer.list.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A [SnackbarHost] themed for this app's undo snackbars (screens 08, 12, 15...). Call
 * [SnackbarHostState.showUndoSnackbar] to show one — it returns true if the user tapped Undo.
 */
@Composable
fun UndoSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            shape = MaterialTheme.shapes.medium,
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionColor = MaterialTheme.colorScheme.inversePrimary,
        )
    }
}

suspend fun SnackbarHostState.showUndoSnackbar(message: String, undoLabel: String = "Undo"): Boolean =
    showSnackbar(message = message, actionLabel = undoLabel) == SnackbarResult.ActionPerformed
