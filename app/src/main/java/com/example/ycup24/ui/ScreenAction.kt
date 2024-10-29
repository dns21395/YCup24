package com.example.ycup24.ui

import androidx.compose.ui.geometry.Offset
import com.example.ycup24.ui.model.Tools

sealed interface ScreenAction {
    data class OnToolClick(val tool: Tools) : ScreenAction
    data class OnDrawLine(val start: Offset, val end: Offset) : ScreenAction
}