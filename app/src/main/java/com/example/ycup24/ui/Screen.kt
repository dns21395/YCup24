package com.example.ycup24.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.Dialog
import com.example.ycup24.R
import com.example.ycup24.core.ui.theme.BrushColorBlue
import com.example.ycup24.core.ui.theme.BrushColorRed
import com.example.ycup24.core.ui.theme.ColorDisabled
import com.example.ycup24.core.ui.theme.ColorSelected
import com.example.ycup24.core.ui.theme.YCup24Theme
import com.example.ycup24.core.ui.theme.colors
import com.example.ycup24.ui.model.Tools
import kotlin.math.roundToInt

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

    if (state.showFrameGeneratorDialog) {
        GenerateFramesDialog(onAction)
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
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .onGloballyPositioned { coordinates ->
                onAction(
                    ScreenAction.OnReceivedDrawerSize(
                        coordinates.size.width,
                        coordinates.size.height,
                    )
                )
            }
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
                    if (state.frames.size > 1 && state.currentFrame != 0) {
                        state.frames[state.currentFrame - 1].forEach { point ->
                            drawCircle(
                                color = Color(point.color),
                                radius = state.currentWidth / 2,
                                center = point.position.toOffset(),
                            )
                        }
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.currentLines.forEach { line ->
                        drawLine(
                            color = Color(state.currentColor),
                            start = line.start.toOffset(),
                            end = line.end.toOffset(),
                            strokeWidth = state.currentWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    state.frames[state.currentFrame].forEach { point ->
                        drawCircle(
                            color = Color(point.color),
                            radius = state.currentWidth / 2,
                            center = point.position.toOffset(),
                        )
                    }
                }
                Text(
                    "Frame #${state.currentFrame + 1} of ${state.frames.size}",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    state.animationPointers.forEach { point ->
                        drawCircle(
                            color = Color(point.color),
                            radius = state.currentWidth / 2,
                            center = point.position.toOffset(),
                        )
                    }
                }
            }
            if (state.isColorPaletteVisible) {
                ColorPalette(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                )
            }
            if (state.selectedTool == Tools.SPEED) {
                SpeedCard(
                    state = state,
                    onAction = onAction,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                )
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
                    tint = if (state.frames.size > 1) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                    modifier = Modifier
                        .size(32.dp)
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
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_frame_list),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAction(ScreenAction.OnFramesGroupClicked) }
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
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnStopAnimationButtonClicked) }
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_play),
                contentDescription = null,
                tint = if (!state.isPlay && state.frames.size > 1) MaterialTheme.colorScheme.onPrimary else ColorDisabled,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnPlayAnimationButtonClicked) }
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
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
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_erase),
                contentDescription = null,
                tint = if (state.selectedTool == Tools.ERASER) ColorSelected else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnToolClick(Tools.ERASER)) }
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_animated_images),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.ShowGenerateFrameDialog) }
            )
            Spacer(Modifier.width(8.dp))
            val context = LocalContext.current
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_duplicate),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        onAction(ScreenAction.DuplicateCurrentFrame)
                        Toast.makeText(context, "previous frame duplicated", Toast.LENGTH_SHORT).show()
                    }
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = state.speedList[state.currentSpeedIndex].first,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable { onAction(ScreenAction.OnToolClick(Tools.SPEED)) },
                color = if (state.selectedTool == Tools.SPEED) ColorSelected else MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (state.selectedTool == Tools.COLOR_PALETTE) ColorSelected else Color(
                            state.currentColor
                        ), shape = CircleShape
                    )
                    .clickable { onAction(ScreenAction.OnToolClick(Tools.COLOR_PALETTE)) }
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color = Color(state.currentColor), shape = CircleShape)
                        .align(Alignment.Center)
                        .clickable { onAction(ScreenAction.OnToolClick(Tools.COLOR_PALETTE)) }
                )
            }
        } else {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ExtraColorPalette(
    onAction: (ScreenAction) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.5f),
        )
    ) {
        for (i in 0 until colors.size / 5) {
            Row(
                Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (j in 0 until 5) {
                    BrushColor(Color(colors[i * 5 + j]), onAction)
                    Spacer(Modifier.width(16.dp))
                }
            }
        }
    }
}


@Composable
fun ColorPalette(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        if (state.isExtraColorPaletteVisible) {
            ExtraColorPalette(onAction)
            Spacer(Modifier.height(16.dp))
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.DarkGray.copy(alpha = 0.5f),
            )
        ) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_palette),
                    contentDescription = null,
                    tint = if (state.isExtraColorPaletteVisible) ColorSelected else Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAction(ScreenAction.OnExtraPaletteClicked) }
                )
                Spacer(Modifier.width(16.dp))
                BrushColor(Color.White, onAction)
                Spacer(Modifier.width(16.dp))
                BrushColor(BrushColorRed, onAction)
                Spacer(Modifier.width(16.dp))
                BrushColor(Color.Black, onAction)
                Spacer(Modifier.width(16.dp))
                BrushColor(BrushColorBlue, onAction)
            }
        }
    }
}

@Composable
fun BrushColor(
    color: Color,
    onAction: (ScreenAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(color = color, shape = CircleShape)
            .clickable { onAction(ScreenAction.OnColorPicked(color.value)) }
    )
}

@Composable
fun SpeedCard(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.8f),
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 0 until state.speedList.size) {
                val (text, speed) = state.speedList[i]
                val currentSpeed = state.speedList[state.currentSpeedIndex].second
                if (speed == currentSpeed) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color = Color.White, shape = CircleShape)
                    ) {
                        Text(
                            text,
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Black
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                } else {
                    Text(
                        text,
                        modifier = Modifier.clickable {
                            onAction(
                                ScreenAction.OnSpeedClickedAction(i)
                            )
                        },
                        color = Color.White
                    )
                    Spacer(Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
fun GenerateFramesDialog(
    onAction: (ScreenAction) -> Unit,
) {
    Dialog(onDismissRequest = { onAction(ScreenAction.ShowGenerateFrameDialog) }) {
        var generatorSize by remember { mutableIntStateOf(1) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary,
            )
        ) {
            Column {
                Text("Generate $generatorSize frames", modifier = Modifier.padding(16.dp))
                Slider(
                    value = generatorSize.toFloat(),
                    onValueChange = { generatorSize = it.roundToInt() },
                    valueRange = 1f..100f,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = { onAction(ScreenAction.ShowGenerateFrameDialog) },
                ) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = { onAction(ScreenAction.GenerateFramesClicked(generatorSize)) },
                ) {
                    Text("Generate")
                }
            }
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