package com.glyphlight.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A full ring of evenly spaced dots, e.g. the halo around the main torch button. */
@Composable
fun DottedRing(
    modifier: Modifier = Modifier,
    color: Color,
    dotCount: Int = 72,
    dotRadiusFraction: Float = 0.014f,
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val dotR = size.minDimension * dotRadiusFraction
        for (i in 0 until dotCount) {
            val angle = (2 * PI * i / dotCount).toFloat()
            val x = center.x + radius * cos(angle)
            val y = center.y + radius * sin(angle)
            drawCircle(color = color, radius = dotR, center = Offset(x, y))
        }
    }
}

/** The glyph three-dot overflow menu that opens Settings. */
@Composable
fun ThreeDotMenu(
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Canvas(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        ),
    ) {
        val dotR = size.minDimension * 0.09f
        val y = size.height / 2f
        val spacing = size.width / 4f
        for (i in 0..2) {
            drawCircle(color = color, radius = dotR, center = Offset(spacing * (i + 1), y))
        }
    }
}

/**
 * A horizontal slider whose track is rendered as a row of dots: solid up to the thumb,
 * dim afterwards. Used for brightness.
 */
@Composable
fun DottedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color(0xFF3A3A3C),
    thumbColor: Color = Color.White,
    dotCount: Int = 32,
    dotFillFraction: Float = 0.55f,
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
        val w = size.width
        val cy = size.height / 2f
        val clamped = value.coerceIn(0f, 1f)
        val thumbX = clamped * w
        val steps = (dotCount - 1).coerceAtLeast(1)
        val spacing = w / steps
        // Radius is derived from the gap between dots (not just track height), so dots
        // never touch regardless of how narrow the slider ends up on a given screen.
        val dotR = minOf(spacing * dotFillFraction / 2f, size.height * 0.5f)
        for (i in 0 until dotCount) {
            val x = w * i / steps
            val color = if (x <= thumbX) activeColor else inactiveColor
            drawCircle(color = color, radius = dotR, center = Offset(x, cy))
        }
        drawCircle(color = thumbColor, radius = size.height * thumbRadiusFraction, center = Offset(thumbX, cy))
    }
}

/** A tiny filled sun made of a core dot plus 8 radiating dot-rays. */
@Composable
fun SunGlyph(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val coreR = size.minDimension * 0.20f
        drawCircle(color = color, radius = coreR, center = Offset(cx, cy))
        val rayInner = coreR + size.minDimension * 0.12f
        val rayOuter = size.minDimension * 0.46f
        val dotR = size.minDimension * 0.045f
        for (i in 0 until 8) {
            val angle = (2 * PI * i / 8).toFloat()
            val x = cx + rayOuter * cos(angle)
            val y = cy + rayOuter * sin(angle)
            drawCircle(color = color, radius = dotR, center = Offset(x, y))
            val xi = cx + rayInner * cos(angle)
            val yi = cy + rayInner * sin(angle)
            drawCircle(color = color, radius = dotR, center = Offset(xi, yi))
        }
    }
}

/** Renders a bitmap-style glyph from rows of characters: any non-space/dot char draws a dot. */
@Composable
fun DotGridGlyph(
    grid: List<String>,
    modifier: Modifier = Modifier,
    color: Color,
    dotRadiusFactor: Float = 0.36f,
) {
    Canvas(modifier = modifier) {
        drawDotGrid(grid, color, dotRadiusFactor)
    }
}

fun DrawScope.drawDotGrid(grid: List<String>, color: Color, dotRadiusFactor: Float = 0.36f) {
    if (grid.isEmpty()) return
    val rows = grid.size
    val cols = grid.maxOf { it.length }
    val cell = minOf(size.width / cols, size.height / rows)
    val gridW = cell * cols
    val gridH = cell * rows
    val offsetX = (size.width - gridW) / 2f
    val offsetY = (size.height - gridH) / 2f
    val dotR = cell * dotRadiusFactor
    for (r in 0 until rows) {
        val row = grid[r]
        for (c in row.indices) {
            val ch = row[c]
            if (ch != ' ' && ch != '.') {
                val x = offsetX + cell * c + cell / 2f
                val y = offsetY + cell * r + cell / 2f
                drawCircle(color = color, radius = dotR, center = Offset(x, y))
            }
        }
    }
}

/**
 * Classic side-view torch silhouette: a 3-ray fan (each ray a real multi-dot
 * stroke, not a single stray dot), a ridged lens head, a tapered neck, a
 * straight body with a button indicator, and a flared base.
 */
val FLASHLIGHT_GRID = listOf(
    "..#...#...#..",
    "...#..#..#...",
    "....#.#.#....",
    ".............",
    "..#########..",
    "..#########..",
    "..#########..",
    "..#########..",
    "..#########..",
    "...#######...",
    "....#####....",
    "....#####....",
    "....#####....",
    "....#####....",
    "....#####....",
    "....#####....",
    "....#####....",
    "....#####....",

    )

/** A simple bed pictogram for the sleep timer dialog. */
@Composable
fun BedGlyph(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val legW = w * 0.06f
        val legH = h * 0.16f
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.10f, h * 0.74f),
            size = Size(legW, legH),
            cornerRadius = CornerRadius(legW / 2f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.84f, h * 0.74f),
            size = Size(legW, legH),
            cornerRadius = CornerRadius(legW / 2f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.30f),
            size = Size(w * 0.06f, h * 0.48f),
            cornerRadius = CornerRadius(w * 0.02f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.55f),
            size = Size(w * 0.84f, h * 0.20f),
            cornerRadius = CornerRadius(h * 0.05f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.13f, h * 0.40f),
            size = Size(w * 0.28f, h * 0.17f),
            cornerRadius = CornerRadius(h * 0.05f),
        )
    }
}

private val S_ROWS = listOf(".###.", "#....", "#....", ".###.", "....#", "....#", ".###.")
private val O_ROWS = listOf(".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.")

/** A 5x7-per-letter dot-matrix rendering of the word "SOS". */
val SOS_GRID = (S_ROWS.indices).map { i -> S_ROWS[i] + "." + O_ROWS[i] + "." + S_ROWS[i] }
