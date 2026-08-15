package com.stackpointer.list.ui.screens.starred

import com.stackpointer.list.domain.model.Item

data class StarredUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
