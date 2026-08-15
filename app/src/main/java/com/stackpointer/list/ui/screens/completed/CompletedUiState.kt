package com.stackpointer.list.ui.screens.completed

import com.stackpointer.list.domain.model.Bucket

data class CompletedUiState(
    val isLoading: Boolean = true,
    val buckets: List<Bucket> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && buckets.isEmpty()
}
