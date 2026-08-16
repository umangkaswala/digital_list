package com.stackpointer.list.domain.model

/** Screen 30's theme row — the design assumes system, so that's the default. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * The DataStore-backed rows from screen 30. Account/sync and the deferred alert rows
 * (dismiss-on-all-devices, shared-collection alerts) aren't modelled here — they're disabled,
 * static rows in the UI per [com.stackpointer.list.Features], not persisted state.
 */
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showPresetTimes: Boolean = true,
    val showPresetPlaces: Boolean = true,
    val defaultAlertType: AlertType = AlertType.MEDIUM,
    val allDayAlertHour: Int = 9,
    val allDayAlertMinute: Int = 0,
)
