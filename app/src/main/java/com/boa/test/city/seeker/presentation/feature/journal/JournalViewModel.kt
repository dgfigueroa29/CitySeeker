package com.boa.test.city.seeker.presentation.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boa.test.city.seeker.domain.usecase.DeleteJournalEntryUseCase
import com.boa.test.city.seeker.domain.usecase.GetAllJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel
@Inject
constructor(
    private val getEntries: GetAllJournalEntriesUseCase,
    private val deleteEntry: DeleteJournalEntryUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getEntries().collectLatest { entries ->
                _state.value = _state.value.copy(entries = entries, loadingState = false)
            }
        }
    }

    fun onDeleteEntry(entryId: Long) {
        viewModelScope.launch {
            deleteEntry(entryId)
        }
    }

    fun onRefresh() {
        _state.value = _state.value.copy(loadingState = true)
        viewModelScope.launch {
            getEntries(forceRefresh = true).collectLatest { entries ->
                _state.value = _state.value.copy(entries = entries, loadingState = false)
            }
        }
    }
}
