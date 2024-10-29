package com.example.ycup24.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Tools
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

class ScreenViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val VAL_ERASER_RADIUS = 20.0f
    }

    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    fun onAction(action: ScreenAction) {
        when (action) {
            is ScreenAction.OnToolClick -> {
                when (action.tool) {
                    Tools.PEN -> _state.update { it.copy(selectedTool = action.tool) }
                    Tools.ERASER -> {
                        _state.update { currentState ->
                            val lines = currentState.currentLines
                            val points = mutableListOf<IntOffset>()
                            for (line in lines) {
                                val pointers = getPoints(line.start, line.end)
                                for (point in pointers) {
                                    points.add(point)
                                }
                            }

                            currentState.copy(
                                currentLines = emptyList(),
                                pointers = points + currentState.pointers,
                                selectedTool = action.tool,
                            )
                        }
                    }
                }
            }

            is ScreenAction.OnDrawLine -> {
                _state.update { currentState ->
                    val currentLines = currentState.currentLines.toMutableList()
                    currentLines.add(Line(action.start.toIntOffset(), action.end.toIntOffset()))

                    currentState.copy(currentLines = currentLines)
                }
            }

            is ScreenAction.OnDrawPoint -> {
                _state.update { currentState ->
                    val pointers = currentState.pointers.toMutableList()
                    pointers.add(action.point.toIntOffset())
                    currentState.copy(pointers = pointers)
                }
            }

            is ScreenAction.OnEraseLine -> {
                val removePoints = getPoints(action.start.toIntOffset(), action.end.toIntOffset())
                _state.update { currentState ->
                    var pointers = currentState.pointers
                    for (point in removePoints) {
                        pointers = pointers.filter { distance(point, it) > VAL_ERASER_RADIUS }
                    }
                    currentState.copy(pointers = pointers)
                }
            }

            is ScreenAction.OnErasePoint -> {
                _state.update { currentState ->
                    val erasePoint = action.point.toIntOffset()
                    val pointers = currentState.pointers.filter {
                        distance(erasePoint, it) > VAL_ERASER_RADIUS
                    }
                    currentState.copy(pointers = pointers)
                }
            }
        }
    }
}

private fun distance(point1: IntOffset, point2: IntOffset): Float {
    val x = (point1.x - point2.x).toFloat()
    val y = (point1.y - point2.y).toFloat()

    return sqrt(x * x + y * y)
}

private fun Offset.toIntOffset(): IntOffset = IntOffset(this.x.toInt(), this.y.toInt())

private fun getPoints(start: IntOffset, end: IntOffset): List<IntOffset> {
    val points = mutableListOf<IntOffset>()

    val dx = end.x - start.x
    val dy = end.y - start.y

    val steps = maxOf(abs(dx), abs(dy))

    val xStep = dx.toFloat() / steps
    val yStep = dy.toFloat() / steps

    var x = start.x.toFloat()
    var y = start.y.toFloat()

    for (i in 0..steps) {
        points.add(IntOffset(x.toInt(), y.toInt()))
        x += xStep
        y += yStep
    }

    return points
}
