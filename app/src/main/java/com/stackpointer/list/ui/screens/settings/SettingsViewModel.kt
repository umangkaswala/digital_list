package com.stackpointer.list.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.model.ThemeMode
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    itemRepository: ItemRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        itemRepository.observeSavedView(SavedView.RECYCLE_BIN),
    ) { settings, recycleBinItems ->
        SettingsUiState(isLoading = false, settings = settings, recycleBinCount = recycleBinItems.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setShowPresetTimes(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowPresetTimes(show) }
    }

    fun setShowPresetPlaces(show: Boolean) {
        viewModelScope.launch { settingsRepository.setShowPresetPlaces(show) }
    }

    fun setDefaultAlertType(type: AlertType) {
        viewModelScope.launch { settingsRepository.setDefaultAlertType(type) }
    }

    fun setAllDayAlertTime(hour: Int, minute: Int) {
        viewModelScope.launch { settingsRepository.setAllDayAlertTime(hour, minute) }
    }
}
