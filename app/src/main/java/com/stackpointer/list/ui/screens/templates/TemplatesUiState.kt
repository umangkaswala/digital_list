package com.stackpointer.list.ui.screens.templates

import com.stackpointer.list.domain.model.Template

data class TemplatesUiState(
    val isLoading: Boolean = true,
    val templates: List<Template> = emptyList(),
)
