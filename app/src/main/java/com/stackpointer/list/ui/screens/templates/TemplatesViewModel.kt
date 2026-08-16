package com.stackpointer.list.ui.screens.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.Template
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val templateRepository: TemplateRepository,
    private val itemRepository: ItemRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { templateRepository.seedIfEmpty() }
    }

    val uiState: StateFlow<TemplatesUiState> = templateRepository.observeAll()
        .map { TemplatesUiState(isLoading = false, templates = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemplatesUiState())

    /** The trailing `add` button — commits the template directly, no review in the capture
     * sheet. Tapping the card itself instead opens the sheet prefilled (see
     * [com.stackpointer.list.ui.screens.capture.CaptureViewModel.openWithDraft]), which this
     * screen's composable drives directly since that ViewModel is screen-scoped. */
    fun applyTemplate(template: Template) {
        viewModelScope.launch { itemRepository.save(template.draft.toItem()) }
    }
}
