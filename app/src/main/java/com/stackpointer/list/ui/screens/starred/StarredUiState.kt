package com.stackpointer.list.ui.screens.starred

import com.stackpointer.list.domain.model.Item

data class StarredUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val collections: List<com.stackpointer.list.domain.model.Collection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
