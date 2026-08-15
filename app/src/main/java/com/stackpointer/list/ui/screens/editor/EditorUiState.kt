package com.stackpointer.list.ui.screens.editor

import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.SubItem
import java.time.Instant

data class EditorUiState(
    val isLoading: Boolean = true,
    val originalItem: Item? = null,
    val title: String = "",
    val body: String = "",
    val isPinned: Boolean = false,
    val isShownInNotificationBar: Boolean = false,
    val isPinnedToNotification: Boolean = false,
    val collections: List<Collection> = emptyList(),
    val allCollections: List<Collection> = emptyList(),
    val subItems: List<SubItem> = emptyList(),
    val lastSavedAt: Instant? = null,
) {
    /** The full item as it currently stands, local edits merged over whatever fields this
     * screen doesn't touch (trigger, recurrence, ...) — what actually gets persisted. */
    fun mergedItem(): Item? = originalItem?.copy(
        title = title,
        body = body.ifBlank { null },
        isPinned = isPinned,
        isShownInNotificationBar = isShownInNotificationBar,
        isPinnedToNotification = isPinnedToNotification,
        collections = collections,
        subItems = subItems,
    )
}
