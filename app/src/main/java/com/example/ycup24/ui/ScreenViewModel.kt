package com.example.ycup24.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ycup24.ui.model.Line
import com.example.ycup24.ui.model.Point
import com.example.ycup24.ui.model.Tools
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sqrt

class ScreenViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val VAL_ERASER_RADIUS = 20.0f
    }

    private val _state = MutableStateFlow(ScreenState())
    val state = _state.asStateFlow()

    private var animationJob: Job? = null

    fun onAction(action: ScreenAction) {
        if (state.value.isColorPaletteVisible && (action !is ScreenAction.OnColorPicked && action !is ScreenAction.OnExtraPaletteClicked)) {
            _state.update { currentState ->
                currentState.copy(
                    selectedTool = Tools.PEN,
                    isColorPaletteVisible = false,
                    isExtraColorPaletteVisible = false
                )
            }
        }

        when (action) {
            is ScreenAction.OnToolClick -> {
                onToolClick(action.tool)
            }

            is ScreenAction.OnDrawLine -> {
                if (state.value.isPlay) {
                    return
                }
                onDrawLine(action.start, action.end)
            }

            is ScreenAction.OnDrawPoint -> {
                if (state.value.isPlay) {
                    return
                }
                onDrawPoint(action.point)
            }

            is ScreenAction.OnEraseLine -> {
                if (state.value.isPlay) {
                    return
                }
                onEraseLine(action.start, action.end)
            }

            is ScreenAction.OnErasePoint -> {
                if (state.value.isPlay) {
                    return
                }
                onErasePoint(action.point)
            }

            is ScreenAction.OnDragEnd -> {
                if (state.value.isPlay) {
                    return
                }
                onDragEnd()
            }

            is ScreenAction.OnActionBackButtonClicked -> {
                onActionBackButtonClicked()
            }

            is ScreenAction.OnActionForwardButtonClicked -> {
                onActionForwardButtonClicked()
            }

            is ScreenAction.OnCreateNewFrameButtonClicked -> {
                onCreateNewFrame()
            }

            is ScreenAction.OnRemoveCurrentFrameButtonClicked -> {
                removeCurrentFrame()
            }

            is ScreenAction.OnPlayAnimationButtonClicked -> {
                if (state.value.isPlay) {
                    return
                }
                playAnimation()
            }

            is ScreenAction.OnStopAnimationButtonClicked -> {
                if (!state.value.isPlay) {
                    return
                }
                stopAnimation()
            }

            is ScreenAction.OnColorPicked -> {
                onColorPicked(action.color)
            }

            is ScreenAction.OnExtraPaletteClicked -> {
                _state.update { it.copy(isExtraColorPaletteVisible = !it.isExtraColorPaletteVisible) }
            }

            is ScreenAction.OnFramesGroupClicked -> {
                _state.update {
                    it.copy(isShowFramesListScreen = !it.isShowFramesListScreen)
                }
            }

            is ScreenAction.OnReceivedDrawerSize -> {
                _state.update {
                    it.copy(
                        previewFrameWidth = action.width * 0.25f,
                        previewFragmentHeight = action.height * 0.25f,
                    )
                }
            }
        }
    }

    private fun onToolClick(tool: Tools) {
        when (tool) {
            Tools.PEN -> _state.update {
                it.copy(selectedTool = tool)
            }

            Tools.ERASER -> {
                _state.update { currentState ->
                    val lines = currentState.currentLines
                    val points = getLinePoints(lines, currentState.currentColor)

                    currentState.copy(
                        currentLines = emptyList(),
                        pointers = points + currentState.pointers,
                        selectedTool = tool,
                    )
                }
            }

            Tools.COLOR_PALETTE -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedTool = tool,
                        isColorPaletteVisible = true
                    )
                }
            }
        }
    }

    private fun onDrawLine(start: Offset, end: Offset) {
        _state.update { currentState ->
            val currentLines = currentState.currentLines.toMutableList()
            currentLines.add(Line(start.toIntOffset(), end.toIntOffset()))

            currentState.copy(currentLines = currentLines)
        }
    }

    private fun onDrawPoint(point: Offset) {
        _state.update { currentState ->
            val pointers = currentState.pointers.toMutableList()
            pointers.add(Point(point.toIntOffset(), currentState.currentColor))
            currentState.copy(
                backActions = currentState.backActions + Pair(
                    Tools.ERASER,
                    listOf(Point(point.toIntOffset(), currentState.currentColor))
                ),
                nextActions = emptyList(),
                pointers = pointers
            )
        }
    }

    private fun onEraseLine(start: Offset, end: Offset) {
        val removePoints = getPoints(start.toIntOffset(), end.toIntOffset())
        val erasePointers = mutableListOf<Point>()

        _state.update { currentState ->
            var pointers = currentState.pointers
            for (point in removePoints) {
                pointers = pointers.filter { value ->
                    val ifNotErase = distance(point, value.position) > VAL_ERASER_RADIUS

                    if (!ifNotErase) {
                        erasePointers.add(value)
                    }

                    ifNotErase
                }
            }
            currentState.copy(
                pointers = pointers,
                erasePointers = currentState.erasePointers + erasePointers,
            )
        }
    }

    private fun onErasePoint(point: Offset) {
        _state.update { currentState ->
            val erasePoint = point.toIntOffset()
            val erasePointers = mutableListOf<Point>()

            val pointers = currentState.pointers.filter { point ->
                val isNotErasing = distance(erasePoint, point.position) > VAL_ERASER_RADIUS
                if (!isNotErasing) {
                    erasePointers.add(point)
                }
                isNotErasing
            }

            if (erasePointers.isNotEmpty()) {
                currentState.copy(
                    backActions = currentState.backActions + Pair(Tools.PEN, erasePointers),
                    nextActions = emptyList(),
                    pointers = pointers,
                )
            } else {
                currentState
            }
        }
    }

    private fun onDragEnd() {
        _state.update { currentState ->
            if (currentState.selectedTool == Tools.PEN) {
                val lines = currentState.currentLines
                val points: List<Point> = getLinePoints(lines, currentState.currentColor)

                currentState.copy(
                    currentLines = emptyList(),
                    pointers = points + currentState.pointers,
                    backActions = currentState.backActions + Pair(Tools.ERASER, points),
                    nextActions = emptyList()
                )
            } else {
                if (currentState.erasePointers.isNotEmpty()) {
                    currentState.copy(
                        erasePointers = emptyList(),
                        backActions = currentState.backActions + Pair(
                            Tools.PEN,
                            currentState.erasePointers
                        ),
                        nextActions = emptyList()
                    )
                } else {
                    currentState
                }
            }
        }
    }

    private fun onActionBackButtonClicked() {
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

    private fun onActionForwardButtonClicked() {
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

    private fun onCreateNewFrame() {
        _state.update { currentState ->
            val frames = currentState.frames.toMutableList()
            frames.add(currentState.pointers)

            currentState.copy(
                backActions = emptyList(),
                nextActions = emptyList(),
                pointers = emptyList(),
                frames = frames,
                currentFrame = currentState.currentFrame + 1
            )
        }
    }

    private fun removeCurrentFrame() {
        _state.update { currentState ->
            if (currentState.frames.isNotEmpty()) {
                val frames = currentState.frames.toMutableList()
                val pointers = frames.last()
                frames.removeLast()

                currentState.copy(
                    frames = frames,
                    pointers = pointers
                )
            } else {
                currentState
            }
        }
    }

    private fun playAnimation() {
        animationJob = viewModelScope.launch {
            val frameDelay = 700L
            val size = state.value.frames.size
            var k = 0
            while (isActive) {
                if (k == size) {
                    _state.update { currentState ->
                        currentState.copy(
                            animationPointers = currentState.pointers
                        )
                    }
                    k = 0
                } else {
                    _state.update { currentState ->
                        currentState.copy(
                            isPlay = true,
                            animationPointers = currentState.frames[k]
                        )
                    }
                    k++
                }
                delay(frameDelay)
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
        _state.update { currentState ->
            currentState.copy(
                isPlay = false,
                animationPointers = emptyList()
            )
        }
    }

    private fun onColorPicked(color: ULong) {
        _state.update { currentState ->
            currentState.copy(
                currentColor = color,
                selectedTool = Tools.PEN,
                isColorPaletteVisible = false,
                isExtraColorPaletteVisible = false,
            )
        }
    }
}

private fun doHistoryAction(
    actionList: List<Pair<Tools, List<Point>>>,
    updateList: List<Pair<Tools, List<Point>>>,
    statePointers: List<Point>
): Triple<List<Pair<Tools, List<Point>>>, List<Pair<Tools, List<Point>>>, List<Point>> {

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


private fun List<Point>.addPointers(
    penPointers: List<Point>,
): List<Point> {
    val array = this.toMutableList()
    for (point in penPointers) {
        array.add(point)
    }

    return array
}

private fun List<Point>.removePointers(
    erasePointers: List<Point>
): List<Point> {
    val array = this.toMutableList()

    for (point in erasePointers) {
        if (point in array) {
            array.remove(point)
        }
    }

    return array
}

private fun getLinePoints(
    lines: List<Line>,
    color: ULong,
): List<Point> {
    val points = mutableListOf<Point>()
    for (line in lines) {
        val pointers = getPoints(line.start, line.end)
        for (point in pointers) {
            points.add(Point(point, color))
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
