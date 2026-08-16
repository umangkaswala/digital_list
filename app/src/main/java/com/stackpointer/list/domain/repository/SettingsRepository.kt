package com.stackpointer.list.domain.repository

import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Settings
import com.stackpointer.list.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setShowPresetTimes(show: Boolean)
    suspend fun setShowPresetPlaces(show: Boolean)
    suspend fun setDefaultAlertType(type: AlertType)
    suspend fun setAllDayAlertTime(hour: Int, minute: Int)
}
