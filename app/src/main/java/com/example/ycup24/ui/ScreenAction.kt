package com.example.ycup24.ui

import androidx.compose.ui.geometry.Offset
import com.example.ycup24.ui.model.Tools

sealed interface ScreenAction {
    data class OnToolClick(val tool: Tools) : ScreenAction
    data class OnDrawLine(val start: Offset, val end: Offset) : ScreenAction
    data object OnDragEnd : ScreenAction
    data class OnDrawPoint(val point: Offset) : ScreenAction
    data class OnEraseLine(val start: Offset, val end: Offset) : ScreenAction
    data class OnErasePoint(val point: Offset) : ScreenAction
    data object OnActionBackButtonClicked : ScreenAction
    data object OnActionForwardButtonClicked : ScreenAction
    data object OnCreateNewFrameButtonClicked : ScreenAction
    data object OnRemoveCurrentFrameButtonClicked : ScreenAction
    data object OnPlayAnimationButtonClicked : ScreenAction
    data object OnStopAnimationButtonClicked : ScreenAction
    data class OnColorPicked(val color: ULong) : ScreenAction
    data object OnExtraPaletteClicked : ScreenAction
}