package com.boa.test.city.seeker.presentation.feature.city.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _stateFlow: MutableStateFlow<ListState> = MutableStateFlow(ListState())

    val stateFlow: StateFlow<ListState> = _stateFlow.asStateFlow()
}
