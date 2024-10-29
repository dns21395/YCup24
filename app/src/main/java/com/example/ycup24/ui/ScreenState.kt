package com.example.ycup24.ui

import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Tools

data class ScreenState(
    val currentFrame: Int = 0,
    val frames: List<List<Line>> = listOf(listOf()),
    val selectedTool: Tools = Tools.PEN
)
