package com.example.ycup24.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import com.example.ycup24.R
import com.example.ycup24.core.ui.theme.ColorDisabled
import com.example.ycup24.core.ui.theme.ColorSelected
import com.example.ycup24.core.ui.theme.YCup24Theme
import com.example.ycup24.ui.model.Tools

@Composable
fun Screen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        UpperRow(state, onAction)
        Drawer(state, onAction, Modifier.weight(1f))
        BottomRow(state, onAction)
    }
}

@Composable
private fun Drawer(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .pointerInput(state.selectedTool) {
                detectTapGestures { offset ->
                    if (state.selectedTool == Tools.PEN) {
                        onAction(ScreenAction.OnDrawPoint(offset))
                    } else {
                        onAction(ScreenAction.OnErasePoint(offset))
                    }
                }
            }
            .pointerInput(state.selectedTool) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = {
                        onAction(ScreenAction.OnDragEnd)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (state.selectedTool == Tools.PEN) {
                            onAction(
                                ScreenAction.OnDrawLine(
                                    change.position - dragAmount,
                                    change.position
                                )
                            )
                        } else {
                            onAction(
                                ScreenAction.OnEraseLine(
                                    change.position - dragAmount,
                                    change.position
                                )
                            )
                        }
                    }
                )
            }

    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painter = painterResource(R.drawable.background),
                    contentScale = ContentScale.Crop
                )
        ) {
            if (!state.isPlay) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f)
                ) {
                    if (state.frames.isNotEmpty()) {
                        state.frames.last().forEach {
                            drawCircle(
                                color = Color.Black,
                                radius = state.currentWidth / 2,
                                center = it.toOffset(),
                            )
                        }
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.currentLines.forEach { line ->
                        drawLine(
                            color = Color.Black,
                            start = line.start.toOffset(),
                            end = line.end.toOffset(),
                            strokeWidth = state.currentWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    state.pointers.forEach {
                        drawCircle(
                            color = Color.Black,
                            radius = state.currentWidth / 2,
                            center = it.toOffset(),
                        )
                    }
                }
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.animationPointers.forEach {
                        drawCircle(
                            color = Color.Black,
                            radius = state.currentWidth / 2,
                            center = it.toOffset(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UpperRow(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        if (!state.isPlay) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = if (state.backActions.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAction(ScreenAction.OnActionBackButtonClicked) }
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_next),
                    contentDescription = null,
                    tint = if (state.nextActions.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAction(ScreenAction.OnActionForwardButtonClicked) }
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_remove_frame),
                    contentDescription = null,
                    tint = if (state.frames.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                    modifier = Modifier.size(32.dp)
                        .clickable { onAction(ScreenAction.OnRemoveCurrentFrameButtonClicked) }
                )
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_create_frame),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAction(ScreenAction.OnCreateNewFrameButtonClicked) }
                )
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_pause),
                contentDescription = null,
                tint = if (state.isPlay) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                modifier = Modifier.size(32.dp).clickable { onAction(ScreenAction.OnStopAnimationButtonClicked) }
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_play),
                contentDescription = null,
                tint = if (!state.isPlay) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                modifier = Modifier.size(32.dp).clickable { onAction(ScreenAction.OnPlayAnimationButtonClicked) }
            )
        }
    }
}

@Composable
private fun BottomRow(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .size(32.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        if (!state.isPlay) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_pen),
                contentDescription = null,
                tint = if (state.selectedTool == Tools.PEN) ColorSelected else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnToolClick(Tools.PEN)) }
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_erase),
                contentDescription = null,
                tint = if (state.selectedTool == Tools.ERASER) ColorSelected else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnToolClick(Tools.ERASER)) }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ScreenPreview() {
    YCup24Theme {
        Surface(
            Modifier.fillMaxSize()
        ) {
            Screen(ScreenState(), {}, Modifier)
        }
    }
}