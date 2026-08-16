package com.stackpointer.list.ui.screens.collections

import com.stackpointer.list.domain.model.CollectionSummary

data class CollectionsUiState(
    val isLoading: Boolean = true,
    val collections: List<CollectionSummary> = emptyList(),
)
