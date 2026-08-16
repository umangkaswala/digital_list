package com.stackpointer.list.ui.screens.settings

import com.stackpointer.list.domain.model.Settings

data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: Settings = Settings(),
    val recycleBinCount: Int = 0,
)
