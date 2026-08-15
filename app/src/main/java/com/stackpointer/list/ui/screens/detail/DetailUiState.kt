package com.stackpointer.list.ui.screens.detail

import com.stackpointer.list.domain.model.Item

data class DetailUiState(
    val isLoading: Boolean = true,
    val item: Item? = null,
)
