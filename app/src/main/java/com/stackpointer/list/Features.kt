package com.stackpointer.list

/**
 * Compile-time flags for the features the handoff defers to a later build (see
 * design-handoff/README.md's "Out of scope for the first build"). Their data-model fields and
 * UI stay in place — this only decides whether they're wired up to do anything yet.
 */
object Features {
    const val placeReminders: Boolean = false
    const val voiceCapture: Boolean = false
    const val imageAttachments: Boolean = false
    const val accountSync: Boolean = false
    const val homeScreenWidget: Boolean = false
}
