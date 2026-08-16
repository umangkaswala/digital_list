package com.stackpointer.list.ui.navigation

/** Flat string routes — plain, since most of these destinations take no arguments. Detail and
 * editor take an itemId path segment. */
object Routes {
    const val HOME = "home"
    const val TODAY = "today"
    const val SCHEDULED = "scheduled"
    const val STARRED = "starred"
    const val NO_ALERT = "no_alert"
    const val COMPLETED = "completed"
    const val SEARCH = "search"
    const val COLLECTIONS = "collections"
    const val TEMPLATES = "templates"
    const val RECYCLE_BIN = "recycle_bin"
    const val SETTINGS = "settings"

    const val DETAIL_PATTERN = "detail/{itemId}"
    const val EDITOR_PATTERN = "editor/{itemId}"

    fun detail(itemId: String) = "detail/$itemId"
    fun editor(itemId: String) = "editor/$itemId"
}

/** A notification tap arriving via [com.stackpointer.list.MainActivity]'s intent extras,
 * naming which item to open and (since the route depends on it) whether it's a note. */
data class ItemDeepLink(val itemId: String, val isNote: Boolean)
