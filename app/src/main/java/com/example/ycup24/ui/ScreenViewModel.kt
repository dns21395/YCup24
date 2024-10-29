package com.example.ycup24.ui

import androidx.lifecycle.ViewModel
import com.example.ycup24.ui.model.Line
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

            is ScreenAction.OnDrawLine -> {
                _state.update { currentState ->
                    val currentFrame = currentState.frames[currentState.currentFrame].toMutableList()
                    currentFrame.add(Line(action.start, action.end))

                    val newFrames = currentState.frames.toMutableList()
                    newFrames[currentState.currentFrame] = currentFrame
                    currentState.copy(frames = newFrames)
                }
            }
        }
    }

}