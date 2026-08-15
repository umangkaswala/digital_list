package com.stackpointer.list.ui.screens.today

import com.stackpointer.list.domain.model.Bucket

data class TodayUiState(
    val isLoading: Boolean = true,
    val doneCount: Int = 0,
    val totalCount: Int = 0,
    val buckets: List<Bucket> = emptyList(),
)
