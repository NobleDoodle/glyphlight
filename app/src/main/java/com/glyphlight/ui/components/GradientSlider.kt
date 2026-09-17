package com.glyphlight.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A slider with a fully custom gradient track and a colored, white-ringed thumb. */
@Composable
fun GradientSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    trackBrush: Brush,
    modifier: Modifier = Modifier,
    thumbColor: Color = Color.White,
    trackHeightFraction: Float = 0.34f,
    thumbRadiusFraction: Float = 0.5f,
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onValueChange((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onValueChange((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                }
            },
    ) {
        val trackH = size.height * trackHeightFraction
        val trackY = size.height / 2f
        drawRoundRect(
            brush = trackBrush,
            topLeft = Offset(0f, trackY - trackH / 2f),
            size = Size(size.width, trackH),
            cornerRadius = CornerRadius(trackH / 2f),
        )
        val thumbX = value.coerceIn(0f, 1f) * size.width
        val thumbR = size.height * thumbRadiusFraction
        drawCircle(color = Color.White, radius = thumbR, center = Offset(thumbX, trackY))
        drawCircle(color = thumbColor, radius = thumbR * 0.72f, center = Offset(thumbX, trackY))
    }
}

/** Standard slider height used across the color picker's H/S/L/R/G/B rows. */
val SliderTrackHeight: Dp = 26.dp
