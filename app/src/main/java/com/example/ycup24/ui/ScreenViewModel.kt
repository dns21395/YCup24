package com.example.ycup24.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class ScreenViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.OnToolClick -> {
                _state.update { it.copy(selectedTool = action.tool) }
            }
        }
    }

}