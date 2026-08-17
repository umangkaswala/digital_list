package com.stackpointer.list.ui.screens.home

import com.stackpointer.list.domain.model.Bucket

data class HomeUiState(
    val isLoading: Boolean = true,
    val todayDone: Int = 0,
    val todayTotal: Int = 0,
    val scheduledCount: Int = 0,
    val starredCount: Int = 0,
    val placeCount: Int = 0,
    val noAlertCount: Int = 0,
    val completedCount: Int = 0,
    val buckets: List<Bucket> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val collections: List<com.stackpointer.list.domain.model.Collection> = emptyList(),
) {
    val isSelectionMode: Boolean get() = selectedIds.isNotEmpty()
}
