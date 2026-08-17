package com.stackpointer.list.domain.usecase

import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import javax.inject.Inject

/**
 * The four bulk actions screen 10's selection-mode top bar exposes (push_pin / label / archive /
 * more_vert-delete), shared by every list screen's ViewModel so the mutation logic isn't
 * duplicated per screen — only the [selectedIds] bookkeeping stays screen-local, since each
 * screen's UiState shape differs.
 */
class BulkSelectionActions @Inject constructor(
    private val itemRepository: ItemRepository,
    private val collectionRepository: CollectionRepository,
) {
    suspend fun pin(selectedIds: Set<String>) {
        selectedIds.forEach { itemRepository.setPinned(it, true) }
    }

    suspend fun archive(selectedIds: Set<String>) {
        selectedIds.forEach { itemRepository.setArchived(it, true) }
    }

    suspend fun addToCollection(selectedIds: Set<String>, collectionId: String) {
        selectedIds.forEach { collectionRepository.addItem(it, collectionId) }
    }

    suspend fun delete(selectedIds: Set<String>) {
        selectedIds.forEach { itemRepository.delete(it) }
    }
}
