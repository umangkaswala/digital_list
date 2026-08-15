package com.stackpointer.list.ui.screens.common

import com.stackpointer.list.domain.repository.UndoToken

/** A one-off "X happened" snackbar with something to undo — emitted by a screen's ViewModel
 * and collected once by the screen (see [com.stackpointer.list.ui.components.showUndoSnackbar]). */
data class UndoEvent(val message: String, val token: UndoToken)
