package com.stackpointer.list.ui.screens.completed

import com.stackpointer.list.domain.model.Bucket

data class CompletedUiState(
    val isLoading: Boolean = true,
    val buckets: List<Bucket> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val collections: List<com.stackpointer.list.domain.model.Collection> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && buckets.isEmpty()
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
