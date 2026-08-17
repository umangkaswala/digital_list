package com.stackpointer.list.ui.screens.noalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.model.TemplateDraft
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.TemplateRepository
import com.stackpointer.list.domain.usecase.BulkSelectionActions
import com.stackpointer.list.ui.screens.common.UndoEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoAlertViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val templateRepository: TemplateRepository,
    collectionRepository: CollectionRepository,
    private val bulkActions: BulkSelectionActions,
) : ViewModel() {

    private val _events = MutableSharedFlow<UndoEvent>()
    val events: SharedFlow<UndoEvent> = _events

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        viewModelScope.launch { templateRepository.seedIfEmpty() }
    }

    val uiState: StateFlow<NoAlertUiState> = combine(
        itemRepository.observeSavedView(SavedView.NO_ALERT),
        templateRepository.observeAll(),
        selectedIds,
        collectionRepository.observeAll(),
    ) { items, templates, selected, collections ->
        NoAlertUiState(
            isLoading = false,
            items = items,
            previewTemplate = templates.firstOrNull(),
            selectedIds = selected,
            collections = collections,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoAlertUiState())

    /** The template card's trailing "add" button commits it directly, per screen 28. */
    fun commitTemplate(draft: TemplateDraft) {
        viewModelScope.launch { itemRepository.save(draft.toItem()) }
    }

    fun completeItem(id: String) {
        viewModelScope.launch {
            val token = itemRepository.complete(id)
            _events.emit(UndoEvent("Task completed", token))
        }
    }

    fun undo(event: UndoEvent) {
        viewModelScope.launch { itemRepository.undo(event.token) }
    }

    fun toggleSelected(id: String) {
        selectedIds.update { if (id in it) it - id else it + id }
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun bulkPin() {
        viewModelScope.launch { bulkActions.pin(selectedIds.value); clearSelection() }
    }

    fun bulkAddToCollection(collectionId: String) {
        viewModelScope.launch { bulkActions.addToCollection(selectedIds.value, collectionId); clearSelection() }
    }

    fun bulkArchive() {
        viewModelScope.launch { bulkActions.archive(selectedIds.value); clearSelection() }
    }

    fun bulkDelete() {
        viewModelScope.launch { bulkActions.delete(selectedIds.value); clearSelection() }
    }
}
