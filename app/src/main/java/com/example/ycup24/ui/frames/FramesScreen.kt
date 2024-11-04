package com.example.ycup24.ui.frames

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import com.example.ycup24.R
import com.example.ycup24.ui.ScreenAction
import com.example.ycup24.ui.ScreenState
import com.example.ycup24.ui.model.Point

@Composable
fun FramesScreen(
    state: ScreenState,
    onAction: (ScreenAction) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAction(ScreenAction.OnFramesGroupClicked) }
            )
            Text(
                text = "Delete all frames",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.clickable { onAction(ScreenAction.RemoveAllFrames) })
        }
        LazyColumn {
            itemsIndexed(state.frames) { index, frame ->
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(ScreenAction.OnFramePicked(index)) }
                ) {
                    Row {
                        FrameCard(
                            circleRadius = state.brushRadius / 2,
                            width = state.previewFrameWidth,
                            height = state.previewFragmentHeight,
                            points = frame,
                            index = index + 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FrameCard(
    index: Int,
    circleRadius: Float,
    width: Float,
    height: Float,
    points: List<Point>,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(with(LocalDensity.current) { height.toDp() })
            .paint(
                painter = painterResource(R.drawable.background),
                contentScale = ContentScale.Crop
            )
    ) {
        Text(
            "Frame #$index", modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            color = Color.Black,
            fontSize = 32.sp
        )

        Canvas(
            modifier = Modifier
                .size(
                    with(LocalDensity.current) { width.toDp() },
                    with(LocalDensity.current) { height.toDp() })
        ) {
            withTransform({
                scale(0.25f, pivot = Offset(64.dp.value, 0.0f))
            }) {
                points.forEach { point ->
                    drawCircle(
                        color = Color(point.color),
                        radius = circleRadius,
                        center = point.position.toOffset(),
                    )
                }
            }
        }
    }
}