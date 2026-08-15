package com.stackpointer.list.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val uiState: StateFlow<DetailUiState> = itemRepository.observeItem(itemId)
        .map { item -> DetailUiState(isLoading = false, item = item) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun toggleStarred() {
        val item = uiState.value.item ?: return
        viewModelScope.launch { itemRepository.setStarred(item.id, !item.isStarred) }
    }

    fun toggleShownInNotificationBar() {
        val item = uiState.value.item ?: return
        viewModelScope.launch { itemRepository.setShownInNotificationBar(item.id, !item.isShownInNotificationBar) }
    }

    fun togglePinnedToNotification() {
        val item = uiState.value.item ?: return
        viewModelScope.launch { itemRepository.setPinnedToNotification(item.id, !item.isPinnedToNotification) }
    }

    fun complete() {
        viewModelScope.launch { itemRepository.complete(itemId) }
    }

    /** Soft-delete only — the item lands in the recycle bin (M8), so there's nothing to
     * offer an immediate undo for here. */
    fun delete() {
        viewModelScope.launch { itemRepository.delete(itemId) }
    }
}
