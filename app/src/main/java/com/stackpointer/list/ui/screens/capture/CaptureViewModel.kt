package com.stackpointer.list.ui.screens.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.AlertType
import com.stackpointer.list.domain.model.Collection
import com.stackpointer.list.domain.model.Item
import com.stackpointer.list.domain.model.Recurrence
import com.stackpointer.list.domain.model.SubItem
import com.stackpointer.list.domain.model.TriggerType
import com.stackpointer.list.domain.repository.CollectionRepository
import com.stackpointer.list.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            collectionRepository.observeAll().collect { collections ->
                _uiState.update { it.copy(allCollections = collections) }
            }
        }
    }

    /** Resets to a fresh draft prefilled per [prefill] and opens the sheet — called by the
     * capture bar's onClick rather than relying on ViewModel recreation, since this
     * ViewModel is scoped to the hosting screen, not to each open/close cycle. */
    fun openFor(prefill: CapturePrefill) {
        val zone = ZoneId.systemDefault()
        val draft = when (prefill) {
            CapturePrefill.NONE -> Item.draft()
            CapturePrefill.TODAY -> Item.draft().copy(
                triggerType = TriggerType.TIME,
                dueAt = LocalDate.now(zone).atTime(9, 0).atZone(zone).toInstant(),
            )
            CapturePrefill.SCHEDULED -> Item.draft().copy(triggerType = TriggerType.TIME)
            CapturePrefill.PLACE -> Item.draft().copy(triggerType = TriggerType.PLACE)
        }
        val mode = when (prefill) {
            CapturePrefill.NONE -> CaptureMode.NONE
            CapturePrefill.TODAY, CapturePrefill.SCHEDULED -> CaptureMode.TIME
            CapturePrefill.PLACE -> CaptureMode.PLACE
        }
        _uiState.update { CaptureUiState(isOpen = true, draft = draft, mode = mode, allCollections = it.allCollections) }
    }

    /** Opens the sheet against an already-persisted item (e.g. the "Add time" chip on an
     * existing no-alert item) rather than a fresh draft — [confirm] then updates it in place,
     * since Room's upsert matches by the item's existing id. */
    fun openForExisting(item: Item, mode: CaptureMode) {
        _uiState.update { CaptureUiState(isOpen = true, draft = item, mode = mode, allCollections = it.allCollections) }
    }

    fun dismiss() {
        _uiState.update { it.copy(isOpen = false) }
    }

    fun updateTitle(title: String) = updateDraft { it.copy(title = title) }

    fun updateBody(body: String) = updateDraft { it.copy(body = body.ifBlank { null }) }

    fun selectMode(mode: CaptureMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    // --- Time mode (screen 18) ---

    fun setAllDay(allDay: Boolean) = updateDraft { it.copy(isAllDay = allDay, triggerType = TriggerType.TIME) }

    fun setDate(date: LocalDate) = updateDraft { item ->
        val zone = ZoneId.systemDefault()
        val time = item.dueAt?.atZone(zone)?.toLocalTime() ?: LocalTime.of(9, 0)
        item.copy(triggerType = TriggerType.TIME, dueAt = date.atTime(time).atZone(zone).toInstant())
    }

    fun setTime(time: LocalTime) = updateDraft { item ->
        val zone = ZoneId.systemDefault()
        val date = item.dueAt?.atZone(zone)?.toLocalDate() ?: LocalDate.now(zone)
        item.copy(triggerType = TriggerType.TIME, dueAt = date.atTime(time).atZone(zone).toInstant())
    }

    fun applyPreset(instant: Instant) =
        updateDraft { it.copy(triggerType = TriggerType.TIME, dueAt = instant, isAllDay = false) }

    fun clearTrigger() = updateDraft { it.copy(triggerType = TriggerType.NONE, dueAt = null, recurrence = null) }

    fun selectEarlyAlert(minutes: Int?) {
        updateDraft { it.copy(earlyAlertMinutes = minutes) }
        dismissEarlyAlertMenu()
    }

    fun setRecurrence(recurrence: Recurrence?) {
        updateDraft { it.copy(recurrence = recurrence) }
        dismissRepeatPicker()
    }

    /** Screen 26 stages a selection behind a "Done" button rather than applying on tap, unlike
     * the early-alert menu — so this both sets the value and closes; the sheet itself holds
     * the in-progress radio selection locally until the user taps Done. */
    fun setAlertType(alertType: AlertType) {
        updateDraft { it.copy(alertType = alertType) }
        dismissAlertTypeSheet()
    }

    fun openEarlyAlertMenu() = _uiState.update { it.copy(earlyAlertMenuOpen = true) }
    fun dismissEarlyAlertMenu() = _uiState.update { it.copy(earlyAlertMenuOpen = false) }
    fun openRepeatPicker() = _uiState.update { it.copy(repeatPickerOpen = true) }
    fun dismissRepeatPicker() = _uiState.update { it.copy(repeatPickerOpen = false) }
    fun openAlertTypeSheet() = _uiState.update { it.copy(alertTypeSheetOpen = true) }
    fun dismissAlertTypeSheet() = _uiState.update { it.copy(alertTypeSheetOpen = false) }
    fun openDatePicker() = _uiState.update { it.copy(datePickerOpen = true) }
    fun dismissDatePicker() = _uiState.update { it.copy(datePickerOpen = false) }
    fun openTimePicker() = _uiState.update { it.copy(timePickerOpen = true) }
    fun dismissTimePicker() = _uiState.update { it.copy(timePickerOpen = false) }

    // --- Checklist mode (screen 22) ---

    fun addSubItem(text: String) {
        if (text.isBlank()) return
        updateDraft { item ->
            val subItem = SubItem(
                id = UUID.randomUUID().toString(),
                itemId = item.id,
                text = text,
                isCompleted = false,
                sortOrder = item.subItems.size,
            )
            item.copy(subItems = item.subItems + subItem)
        }
    }

    fun toggleSubItem(id: String) = updateDraft { item ->
        item.copy(subItems = item.subItems.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it })
    }

    fun removeSubItem(id: String) = updateDraft { item ->
        item.copy(subItems = item.subItems.filterNot { it.id == id })
    }

    fun updateSubItemText(id: String, text: String) = updateDraft { item ->
        item.copy(subItems = item.subItems.map { if (it.id == id) it.copy(text = text) else it })
    }

    // --- Label / collection mode (screen 23) ---

    fun toggleCollection(collection: Collection) = updateDraft { item ->
        val alreadyIn = item.collections.any { it.id == collection.id }
        item.copy(
            collections = if (alreadyIn) item.collections.filterNot { it.id == collection.id } else item.collections + collection,
        )
    }

    fun createCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val collection = Collection(
                id = UUID.randomUUID().toString(),
                name = name,
                iconKey = "label",
                colorKey = null,
                isShared = false,
                sortOrder = uiState.value.allCollections.size,
            )
            collectionRepository.save(collection)
            updateDraft { it.copy(collections = it.collections + collection) }
        }
    }

    fun confirm() {
        val draft = uiState.value.draft
        if (draft.title.isBlank()) return
        viewModelScope.launch {
            itemRepository.save(draft.copy(updatedAt = Instant.now()))
            dismiss()
        }
    }

    private inline fun updateDraft(transform: (Item) -> Item) {
        _uiState.update { it.copy(draft = transform(it.draft)) }
    }
}
