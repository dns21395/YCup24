package com.example.ycup24.ui

import androidx.compose.ui.unit.IntOffset
import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Tools

data class ScreenState(
    val currentFrame: Int = 0,
    val currentWidth: Float = 15.0f,
    val backActions: List<Pair<Tools, List<IntOffset>>> = emptyList(),
    val nextActions: List<Pair<Tools, List<IntOffset>>> = emptyList(),
    val erasePointers: List<IntOffset> = emptyList(),
    val currentLines: List<Line> = emptyList(),
    val pointers: List<IntOffset> = emptyList(),
    val selectedTool: Tools = Tools.PEN,
    val frames: List<List<IntOffset>> = emptyList(),
)
