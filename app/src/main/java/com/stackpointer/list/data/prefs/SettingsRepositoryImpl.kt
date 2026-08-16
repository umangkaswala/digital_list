package com.stackpointer.list.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Settings
import com.stackpointer.list.domain.model.ThemeMode
import com.stackpointer.list.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            themeMode = prefs[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            showPresetTimes = prefs[SHOW_PRESET_TIMES] ?: true,
            showPresetPlaces = prefs[SHOW_PRESET_PLACES] ?: true,
            defaultAlertType = prefs[DEFAULT_ALERT_TYPE]?.let { runCatching { AlertType.valueOf(it) }.getOrNull() } ?: AlertType.MEDIUM,
            allDayAlertHour = prefs[ALL_DAY_ALERT_HOUR] ?: 9,
            allDayAlertMinute = prefs[ALL_DAY_ALERT_MINUTE] ?: 0,
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = mode.name }
    }

    override suspend fun setShowPresetTimes(show: Boolean) {
        dataStore.edit { it[SHOW_PRESET_TIMES] = show }
    }

    override suspend fun setShowPresetPlaces(show: Boolean) {
        dataStore.edit { it[SHOW_PRESET_PLACES] = show }
    }

    override suspend fun setDefaultAlertType(type: AlertType) {
        dataStore.edit { it[DEFAULT_ALERT_TYPE] = type.name }
    }

    override suspend fun setAllDayAlertTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[ALL_DAY_ALERT_HOUR] = hour
            it[ALL_DAY_ALERT_MINUTE] = minute
        }
    }

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SHOW_PRESET_TIMES = booleanPreferencesKey("show_preset_times")
        val SHOW_PRESET_PLACES = booleanPreferencesKey("show_preset_places")
        val DEFAULT_ALERT_TYPE = stringPreferencesKey("default_alert_type")
        val ALL_DAY_ALERT_HOUR = intPreferencesKey("all_day_alert_hour")
        val ALL_DAY_ALERT_MINUTE = intPreferencesKey("all_day_alert_minute")
    }
}
