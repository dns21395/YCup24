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
                            val points = getLinePoints(lines)

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
                    currentState.copy(
                        backActions = currentState.backActions + Pair(Tools.ERASER, listOf(action.point.toIntOffset())),
                        nextActions = emptyList(),
                        pointers = pointers
                    )
                }
            }

            is ScreenAction.OnEraseLine -> {
                val removePoints = getPoints(action.start.toIntOffset(), action.end.toIntOffset())
                val erasePointers = mutableListOf<IntOffset>()

                _state.update { currentState ->
                    var pointers = currentState.pointers
                    for (point in removePoints) {
                        pointers = pointers.filter { value ->
                            val ifNotErase = distance(point, value) > VAL_ERASER_RADIUS

                            if (!ifNotErase) {
                                erasePointers.add(value)
                            }

                            ifNotErase
                        }
                    }
                    currentState.copy(
                        pointers = pointers,
                        erasePointers = currentState.erasePointers + erasePointers,
                        nextActions = emptyList()
                    )
                }
            }

            is ScreenAction.OnErasePoint -> {
                _state.update { currentState ->
                    val erasePoint = action.point.toIntOffset()
                    val erasePointers = mutableListOf<IntOffset>()

                    val pointers = currentState.pointers.filter { point ->
                        val isNotErasing = distance(erasePoint, point) > VAL_ERASER_RADIUS
                        if (!isNotErasing) {
                            erasePointers.add(point)
                        }
                        isNotErasing
                    }
                    currentState.copy(
                        backActions = currentState.backActions + Pair(Tools.PEN, erasePointers),
                        nextActions = emptyList(),
                        pointers = pointers,
                    )
                }
            }

            is ScreenAction.OnDragEnd -> {
                _state.update { currentState ->
                    if (currentState.selectedTool == Tools.PEN) {
                        val lines = currentState.currentLines
                        val points: List<IntOffset> = getLinePoints(lines)

                        currentState.copy(
                            currentLines = emptyList(),
                            pointers = points + currentState.pointers,
                            backActions = currentState.backActions + Pair(Tools.ERASER, points),
                            nextActions = emptyList()
                        )
                    } else {
                        currentState.copy(
                            erasePointers = emptyList(),
                            backActions = currentState.backActions + Pair(
                                Tools.PEN,
                                currentState.erasePointers
                            ),
                            nextActions = emptyList()
                        )
                    }
                }
            }

            is ScreenAction.OnActionBackButtonClicked -> {
                _state.update { currentState ->
                    val backActions = currentState.backActions.toMutableList()
                    val nextActions = currentState.nextActions.toMutableList()
                    if (backActions.isNotEmpty()) {
                        val tripe = doHistoryAction(
                            actionList = backActions,
                            updateList = nextActions,
                            statePointers = currentState.pointers
                        )
                        currentState.copy(
                            pointers = tripe.third,
                            backActions = tripe.first,
                            nextActions = tripe.second
                        )

                    } else {
                        currentState
                    }
                }
            }

            is ScreenAction.OnActionForwardButtonClicked -> {
                _state.update { currentState ->
                    val backActions = currentState.backActions.toMutableList()
                    val nextActions = currentState.nextActions.toMutableList()
                    if (nextActions.isNotEmpty()) {
                        val tripe = doHistoryAction(
                            actionList = nextActions,
                            updateList = backActions,
                            statePointers = currentState.pointers
                        )
                        currentState.copy(
                            pointers = tripe.third,
                            backActions = tripe.second,
                            nextActions = tripe.first
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }
}

private fun doHistoryAction(
    actionList: List<Pair<Tools, List<IntOffset>>>,
    updateList: List<Pair<Tools, List<IntOffset>>>,
    statePointers: List<IntOffset>
): Triple<List<Pair<Tools, List<IntOffset>>>, List<Pair<Tools, List<IntOffset>>>, List<IntOffset>> {

    val mActionList = actionList.toMutableList()
    val mUpdateList = updateList.toMutableList()

    val nextAction = mActionList.last()
    mActionList.removeLast()

    val actionTool = nextAction.first
    val actionPointers = nextAction.second

    val pointers = if (actionTool == Tools.ERASER) {
        mUpdateList.add(Pair(Tools.PEN, actionPointers))
        statePointers.removePointers(actionPointers)
    } else {
        mUpdateList.add(Pair(Tools.ERASER, actionPointers))
        statePointers.addPointers(actionPointers)
    }

    return Triple(
        mActionList,
        mUpdateList,
        pointers
    )
}


private fun List<IntOffset>.addPointers(
    penPointers: List<IntOffset>,
): List<IntOffset> {
    val array = this.toMutableList()
    for (point in penPointers) {
        array.add(point)
    }

    return array
}

private fun List<IntOffset>.removePointers(
    erasePointers: List<IntOffset>
): List<IntOffset> {
    val array = this.toMutableList()

    for (point in erasePointers) {
        if (point in array) {
            array.remove(point)
        }
    }

    return array
}

private fun getLinePoints(lines: List<Line>): List<IntOffset> {
    val points = mutableListOf<IntOffset>()
    for (line in lines) {
        val pointers = getPoints(line.start, line.end)
        for (point in pointers) {
            points.add(point)
        }
    }

    return points
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
