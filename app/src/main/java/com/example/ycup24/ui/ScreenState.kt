package com.example.ycup24.ui

import com.example.ycup24.core.ui.theme.BrushColorBlue
import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Point
import com.example.ycup24.ui.model.Tools

data class ScreenState(
    val currentFrame: Int = 0,
    val currentWidth: Float = 15.0f,
    val currentColor: ULong = BrushColorBlue.value,
    val backActions: List<Pair<Tools, List<Point>>> = emptyList(),
    val nextActions: List<Pair<Tools, List<Point>>> = emptyList(),
    val erasePointers: List<Point> = emptyList(),
    val currentLines: List<Line> = emptyList(),
    val pointers: List<Point> = emptyList(),
    val selectedTool: Tools = Tools.PEN,
    val frames: List<List<Point>> = emptyList(),
    val isPlay: Boolean = false,
    val animationPointers: List<Point> = emptyList(),
    val isColorPaletteVisible: Boolean = false,
    val isExtraColorPaletteVisible: Boolean = false,
)
