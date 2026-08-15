package com.stackpointer.list.ui.screens.scheduled

import com.stackpointer.list.domain.model.Bucket
import com.stackpointer.list.ui.screens.capture.SortOrder

data class ScheduledUiState(
    val isLoading: Boolean = true,
    val buckets: List<Bucket> = emptyList(),
    val sortOrder: SortOrder = SortOrder.DUE_DATE,
) {
    val isEmpty: Boolean get() = !isLoading && buckets.isEmpty()
}
