package com.stackpointer.list.ui.screens.capture

import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item

data class CaptureUiState(
    val isOpen: Boolean = false,
    val draft: Item = Item.draft(),
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
