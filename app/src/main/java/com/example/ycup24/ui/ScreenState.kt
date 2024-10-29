package com.example.ycup24.ui

import androidx.compose.ui.unit.IntOffset
import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Tools

data class ScreenState(
    val currentFrame: Int = 0,
    val currentWidth: Float = 15.0f,
    val currentLines: List<Line> = emptyList(),
    val frames: List<List<IntOffset>> = listOf(listOf()),
    val pointers: List<IntOffset> = emptyList(),
    val selectedTool: Tools = Tools.PEN
)
