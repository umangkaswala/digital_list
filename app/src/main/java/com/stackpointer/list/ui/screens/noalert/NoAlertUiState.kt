package com.stackpointer.list.ui.screens.noalert

import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Template

data class NoAlertUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    val previewTemplate: Template? = null,
    val selectedIds: Set<String> = emptySet(),
    val collections: List<com.stackpointer.list.domain.model.Collection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
