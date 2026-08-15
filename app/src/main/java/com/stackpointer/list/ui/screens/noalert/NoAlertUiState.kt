package com.stackpointer.list.ui.screens.noalert

import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Template

data class NoAlertUiState(
    val isLoading: Boolean = true,
    val items: List<Item> = emptyList(),
    val previewTemplate: Template? = null,
) {
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
