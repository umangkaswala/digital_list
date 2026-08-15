package com.stackpointer.list.ui.navigation

/** Flat string routes — plain, since none of M4's destinations take arguments yet. Detail/
 * editor (M6) will need an itemId argument; revisit as a typed route then if it's worth it. */
object Routes {
    const val HOME = "home"
    const val TODAY = "today"
    const val SCHEDULED = "scheduled"
    const val STARRED = "starred"
    const val NO_ALERT = "no_alert"
    const val COMPLETED = "completed"
}
