package com.stackpointer.list.ui.screens.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.SubItem
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

private const val AUTOSAVE_DEBOUNCE_MS = 600L

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        // Loaded once, not continuously collected — after this, local edits are the source of
        // truth for what's on screen, and re-collecting Room's Flow while the user is typing
        // would fight with that.
        viewModelScope.launch {
            val item = itemRepository.observeItem(itemId).filterNotNull().first()
            _uiState.value = EditorUiState(
                isLoading = false,
                originalItem = item,
                title = item.title,
                body = item.body.orEmpty(),
                isPinned = item.isPinned,
                isShownInNotificationBar = item.isShownInNotificationBar,
                isPinnedToNotification = item.isPinnedToNotification,
                collections = item.collections,
                subItems = item.subItems,
                lastSavedAt = item.updatedAt,
            )
        }
        viewModelScope.launch {
            collectionRepository.observeAll().collect { all ->
                _uiState.update { it.copy(allCollections = all) }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
        scheduleAutosave()
    }

    fun updateBody(body: String) {
        _uiState.update { it.copy(body = body) }
        scheduleAutosave()
    }

    fun togglePinned() {
        _uiState.update { it.copy(isPinned = !it.isPinned) }
        scheduleAutosave(immediate = true)
    }

    fun toggleShownInNotificationBar() {
        _uiState.update { it.copy(isShownInNotificationBar = !it.isShownInNotificationBar) }
        scheduleAutosave(immediate = true)
    }

    fun togglePinnedToNotification() {
        _uiState.update { it.copy(isPinnedToNotification = !it.isPinnedToNotification) }
        scheduleAutosave(immediate = true)
    }

    fun addChecklistItem() {
        _uiState.update { state ->
            val subItem = SubItem(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                text = "",
                isCompleted = false,
                sortOrder = state.subItems.size,
            )
            state.copy(subItems = state.subItems + subItem)
        }
        scheduleAutosave(immediate = true)
    }

    fun updateChecklistItemText(id: String, text: String) {
        _uiState.update { state ->
            state.copy(subItems = state.subItems.map { if (it.id == id) it.copy(text = text) else it })
        }
        scheduleAutosave()
    }

    fun toggleChecklistItem(id: String) {
        _uiState.update { state ->
            state.copy(subItems = state.subItems.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it })
        }
        scheduleAutosave(immediate = true)
    }

    fun toggleCollection(collection: Collection) {
        _uiState.update { state ->
            val alreadyIn = state.collections.any { it.id == collection.id }
            state.copy(
                collections = if (alreadyIn) state.collections.filterNot { it.id == collection.id } else state.collections + collection,
            )
        }
        scheduleAutosave(immediate = true)
    }

    private fun scheduleAutosave(immediate: Boolean = false) {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            if (!immediate) delay(AUTOSAVE_DEBOUNCE_MS)
            val item = _uiState.value.mergedItem() ?: return@launch
            val now = Instant.now()
            itemRepository.save(item.copy(updatedAt = now))
            _uiState.update { it.copy(lastSavedAt = now) }
        }
    }
}
