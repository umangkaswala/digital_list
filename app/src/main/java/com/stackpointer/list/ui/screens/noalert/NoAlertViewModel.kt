package com.stackpointer.list.ui.screens.noalert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stackpointer.list.domain.model.SavedView
import com.stackpointer.list.domain.repository.ItemRepository
import com.stackpointer.list.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoAlertViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val templateRepository: TemplateRepository,
) : ViewModel() {

    init {
        viewModelScope.launch { templateRepository.seedIfEmpty() }
    }

    val uiState: StateFlow<NoAlertUiState> = combine(
        itemRepository.observeSavedView(SavedView.NO_ALERT),
        templateRepository.observeAll(),
    ) { items, templates ->
        NoAlertUiState(isLoading = false, items = items, previewTemplate = templates.firstOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoAlertUiState())
}
