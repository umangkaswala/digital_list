package com.stackpointer.list.ui.screens.today

import com.stackpointer.list.domain.model.Bucket

data class TodayUiState(
    val isLoading: Boolean = true,
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val buckets: List<Bucket> = emptyList(),
    // Screen 10's selection mode — non-empty means the list is in selection mode.
    val selectedIds: Set<String> = emptySet(),
    val collections: List<com.stackpointer.list.domain.model.Collection> = emptyList(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
