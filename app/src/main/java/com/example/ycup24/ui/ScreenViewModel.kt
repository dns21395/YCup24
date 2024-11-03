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
        private const val VAL_ERASER_RADIUS = 25.0f
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

        if (state.value.selectedTool == Tools.SPEED && action !is ScreenAction.OnToolClick) {
            _state.update { it.copy(selectedTool = Tools.PEN) }
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

            is ScreenAction.OnFramePicked -> {
                _state.update {
                    it.copy(
                        currentFrame = action.position,
                        isShowFramesListScreen = false
                    )
                }
            }

            is ScreenAction.DuplicateCurrentFrame -> {
                _state.update { currentState ->
                    val frames = currentState.frames.toMutableList()
                    val points = currentState.frames[currentState.currentFrame]
                    frames.add(currentState.currentFrame + 1, points)

                    currentState.copy(
                        frames = frames,
                        currentFrame = currentState.currentFrame + 1,
                    )
                }
            }

            is ScreenAction.RemoveAllFrames -> {
                _state.update { currentState ->
                    ScreenState().copy(
                        previewFrameWidth = currentState.previewFrameWidth,
                        previewFragmentHeight = currentState.previewFragmentHeight,
                        currentColor = currentState.currentColor,
                        isShowFramesListScreen = true
                    )
                }
            }

            is ScreenAction.OnSpeedClickedAction -> {
                _state.update { currentState ->
                    currentState.copy(
                        currentSpeed = action.speed
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

                    val frames = currentState.frames.toMutableList()
                    val framePointers = frames[currentState.currentFrame].toMutableList()
                    val points = getLinePoints(lines, currentState.currentColor)
                    framePointers.addAll(points)
                    frames[currentState.currentFrame] = framePointers

                    currentState.copy(
                        currentLines = emptyList(),
                        frames = frames,
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

            Tools.SPEED -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedTool = if (currentState.selectedTool == Tools.SPEED) Tools.PEN else Tools.SPEED
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
        addPointToCurrentFrame(Point(point.toIntOffset(), state.value.currentColor))

        _state.update { currentState ->
            currentState.copy(
                backActions = currentState.backActions + Pair(
                    Tools.ERASER,
                    listOf(Point(point.toIntOffset(), currentState.currentColor))
                ),
                nextActions = emptyList(),
            )
        }
    }

    private fun onEraseLine(start: Offset, end: Offset) {
        val removePoints = getPoints(start.toIntOffset(), end.toIntOffset())
        removePointsFromCurrentFrame(removePoints)
    }

    private fun onErasePoint(point: Offset) {
        val erasePoint = point.toIntOffset()
        removePointFromCurrentFrame(erasePoint)
    }

    private fun onDragEnd() {
        val points: List<Point> = getLinePoints(state.value.currentLines, state.value.currentColor)
        if (state.value.selectedTool == Tools.PEN) {
            addPointsToCurrentFrame(points)
        }

        _state.update { currentState ->
            if (currentState.selectedTool == Tools.PEN) {
                currentState.copy(
                    currentLines = emptyList(),
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
                val triple = doHistoryAction(
                    actionList = backActions,
                    updateList = nextActions,
                    statePointers = currentState.frames[currentState.currentFrame]
                )
                currentState.copy(
                    frames = updatePointersInFrames(
                        currentState.frames,
                        currentState.currentFrame,
                        triple.third
                    ),
                    backActions = triple.first,
                    nextActions = triple.second
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
                val triple = doHistoryAction(
                    actionList = nextActions,
                    updateList = backActions,
                    statePointers = currentState.frames[currentState.currentFrame]
                )
                currentState.copy(
                    frames = updatePointersInFrames(
                        currentState.frames,
                        currentState.currentFrame,
                        triple.third
                    ),
                    backActions = triple.second,
                    nextActions = triple.first
                )
            } else {
                currentState
            }
        }
    }

    private fun onCreateNewFrame() {
        _state.update { currentState ->
            val frames = currentState.frames.toMutableList()
            frames.add(currentState.currentFrame + 1, emptyList())

            currentState.copy(
                backActions = emptyList(),
                nextActions = emptyList(),
                frames = frames,
                currentFrame = currentState.currentFrame + 1
            )
        }
    }

    private fun removeCurrentFrame() {
        _state.update { currentState ->
            if (currentState.frames.size > 1) {
                val frames = currentState.frames.toMutableList()
                frames.removeAt(currentState.currentFrame)

                currentState.copy(
                    currentFrame = if (currentState.currentFrame == 0) 0 else currentState.currentFrame - 1,
                    frames = frames
                )
            } else {
                currentState
            }
        }
    }

    private fun playAnimation() {
        animationJob = viewModelScope.launch {
            val frameDelay = 400L * state.value.currentSpeed
            val size = state.value.frames.size
            var k = 0
            while (isActive) {
                if (k == size) {
                    k = 0
                    _state.update { currentState ->
                        currentState.copy(
                            isPlay = true,
                            animationPointers = currentState.frames[k]
                        )
                    }
                    k++
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

    private fun addPointToCurrentFrame(
        point: Point
    ) {
        addPointsToCurrentFrame(listOf(point))
        _state.update { currentState ->
            currentState.copy(
                backActions = currentState.backActions + Pair(Tools.ERASER, listOf(point)),
                nextActions = emptyList()
            )
        }
    }

    private fun addPointsToCurrentFrame(
        points: List<Point>
    ) {
        _state.update { currentState ->
            val frames = currentState.frames.toMutableList()
            val pointers = frames[currentState.currentFrame].toMutableList()
            pointers.addAll(points)
            frames[currentState.currentFrame] = pointers

            currentState.copy(frames = frames)
        }
    }

    private fun removePointFromCurrentFrame(
        point: IntOffset
    ) {
        removePointsFromCurrentFrame(listOf(point))
        _state.update { currentState ->
            currentState.copy(
                backActions = currentState.backActions + Pair(
                    Tools.PEN,
                    listOf(Point(point, currentState.currentColor))
                ),
                nextActions = emptyList()
            )
        }
    }

    private fun removePointsFromCurrentFrame(
        points: List<IntOffset>
    ) {
        _state.update { currentState ->
            val erasePointers = mutableListOf<Point>()
            val frames = currentState.frames.toMutableList()
            var framePointers = frames[currentState.currentFrame]
            for (point in points) {
                framePointers = framePointers.filter { value ->
                    val ifNotErase = distance(point, value.position) > VAL_ERASER_RADIUS

                    if (!ifNotErase) {
                        erasePointers.add(value)
                    }

                    ifNotErase
                }
            }
            frames[currentState.currentFrame] = framePointers

            if (erasePointers.isNotEmpty()) {
                currentState.copy(
                    frames = frames,
                    erasePointers = currentState.erasePointers + erasePointers
                )
            } else {
                currentState
            }
        }
    }

    private fun updatePointersInFrames(
        frames: List<List<Point>>,
        currentFrame: Int,
        pointers: List<Point>
    ): List<List<Point>> {
        val _frames = frames.toMutableList()
        _frames[currentFrame] = pointers
        return _frames
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
