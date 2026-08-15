package com.stackpointer.list.ui.screens.scheduled

import com.stackpointer.list.domain.model.Bucket

data class ScheduledUiState(
    val isLoading: Boolean = true,
    val buckets: List<Bucket> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && buckets.isEmpty()
}
