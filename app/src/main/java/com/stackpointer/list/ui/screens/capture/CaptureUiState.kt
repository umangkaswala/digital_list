package com.stackpointer.list.ui.screens.capture

import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item

data class CaptureUiState(
    val isOpen: Boolean = false,
    val draft: Item = Item.draft(),
    /** True only when [draft] is already a row in Room (opened via `openForExisting`) — a
     * fresh draft from `openFor` has an id but nothing to react to yet, so the notification-bar
     * overflow stays disabled until the first save gives it something persisted to toggle. */
    val isPersisted: Boolean = false,
    val mode: CaptureMode = CaptureMode.NONE,
    val allCollections: List<Collection> = emptyList(),
    val earlyAlertMenuOpen: Boolean = false,
    val repeatPickerOpen: Boolean = false,
    val alertTypeSheetOpen: Boolean = false,
    val datePickerOpen: Boolean = false,
    val timePickerOpen: Boolean = false,
) {
    val canConfirm: Boolean get() = draft.title.isNotBlank()
}
