package com.example.ycup24.ui

import com.example.ycup24.ui.model.Tools

sealed interface ScreenAction {
    data class OnToolClick(val tool: Tools) : ScreenAction
}