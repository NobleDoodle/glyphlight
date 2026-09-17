package com.glyphlight.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphlight.TorchViewModel
import com.glyphlight.ui.components.ColorSwatch
import com.glyphlight.ui.components.GradientSlider
import com.glyphlight.util.blueInt
import com.glyphlight.util.colorToHex
import com.glyphlight.util.colorToHsl
import com.glyphlight.util.greenInt
import com.glyphlight.util.hexToColorOrNull
import com.glyphlight.util.hslToColor
import com.glyphlight.util.redInt
import com.glyphlight.util.rgbInts
import kotlin.math.roundToInt

private enum class ColorMode { HSL, RGB }

private val PRESET_COLORS = listOf(
    0xFFFFFFFFL, 0xFFE8352BL, 0xFFFF9500L,
    0xFF34C759L, 0xFF32D1C4L, 0xFF2E7CF6L, 0xFFBF39F5L,
).map { Color(it) }

@Composable
fun ColorPickerScreen(viewModel: TorchViewModel) {
    // H/S/L and R/G/B are independent, authoritative state — NOT re-derived from the resulting
    // Color on every recomposition. Round-tripping Color -> HSL is numerically unstable (hue is
    // near-undefined at low saturation, and degenerate at L=0/L=1), and 8-bit RGB rounding
    // amplifies that noise. Re-deriving it every frame while dragging one slider made the other
    // sliders visibly jitter/jump. Instead, each tab owns its own values, and the two only ever
    // get resynced on deliberate, one-off actions (switching tabs, typing a hex code, tapping a
    // preset/saved swatch) — never continuously during a drag.
    val initialColor = remember { viewModel.editingColor }
    val initialHsl = remember { colorToHsl(initialColor) }

    var mode by remember { mutableStateOf(ColorMode.HSL) }
    var deleteMode by remember { mutableStateOf(false) }

    var hue by remember { mutableFloatStateOf(initialHsl[0]) }
    var sat by remember { mutableFloatStateOf(initialHsl[1]) }
    var light by remember { mutableFloatStateOf(initialHsl[2]) }

    var r by remember { mutableIntStateOf(initialColor.redInt()) }
    var g by remember { mutableIntStateOf(initialColor.greenInt()) }
    var b by remember { mutableIntStateOf(initialColor.blueInt()) }

    val color = if (mode == ColorMode.HSL) hslToColor(hue, sat, light) else rgbInts(r, g, b)
    var hexField by remember(color) { mutableStateOf(colorToHex(color)) }

    fun pushHsl(h: Float = hue, s: Float = sat, l: Float = light) {
        hue = h
        sat = s
        light = l
        viewModel.updateEditingColor(hslToColor(h, s, l))
    }
    fun pushRgb(rr: Int = r, gg: Int = g, bb: Int = b) {
        r = rr
        g = gg
        b = bb
        viewModel.updateEditingColor(rgbInts(rr, gg, bb))
    }
    fun setColor(newColor: Color) {
        val newHsl = colorToHsl(newColor)
        hue = newHsl[0]
        sat = newHsl[1]
        light = newHsl[2]
        r = newColor.redInt()
        g = newColor.greenInt()
        b = newColor.blueInt()
        viewModel.updateEditingColor(newColor)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(color),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = viewModel::closeToHome) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { viewModel.saveEditingColorToPalette() }) {
                Text("SAVE", color = Color(0xFFFFD60A), fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { viewModel.applyEditingColor() }) {
                Text("APPLY", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
            ModeToggle(
                mode = mode,
                onModeChange = { newMode ->
                    if (newMode != mode) {
                        // One-time sync into the tab being switched to, using whichever
                        // representation was authoritative in the tab being left.
                        if (newMode == ColorMode.RGB) {
                            r = color.redInt()
                            g = color.greenInt()
                            b = color.blueInt()
                        } else {
                            val hslNow = colorToHsl(color)
                            hue = hslNow[0]
                            sat = hslNow[1]
                            light = hslNow[2]
                        }
                        mode = newMode
                    }
                },
            )
        }

        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)) {
            if (mode == ColorMode.HSL) {
                val hueBrush = remember {
                    Brush.horizontalGradient((0..12).map { hslToColor(it * 30f, 1f, 0.5f) })
                }
                LabeledSlider(
                    label = "H",
                    value = hue / 360f,
                    valueText = hue.roundToInt().toString(),
                    brush = hueBrush,
                    thumbColor = color,
                    onValueChange = { pushHsl(h = it * 360f) },
                )
                val satBrush = remember(hue, light) {
                    Brush.horizontalGradient(listOf(hslToColor(hue, 0f, light), hslToColor(hue, 1f, light)))
                }
                LabeledSlider(
                    label = "S",
                    value = sat,
                    valueText = (sat * 100).roundToInt().toString(),
                    brush = satBrush,
                    thumbColor = color,
                    onValueChange = { pushHsl(s = it) },
                )
                val lightBrush = remember(hue, sat) {
                    Brush.horizontalGradient(
                        listOf(Color.Black, hslToColor(hue, sat, 0.5f), Color.White),
                    )
                }
                LabeledSlider(
                    label = "L",
                    value = light,
                    valueText = (light * 100).roundToInt().toString(),
                    brush = lightBrush,
                    thumbColor = color,
                    onValueChange = { pushHsl(l = it) },
                )
            } else {
                val rBrush = remember(g, b) {
                    Brush.horizontalGradient(listOf(rgbInts(0, g, b), rgbInts(255, g, b)))
                }
                LabeledSlider(
                    label = "R",
                    value = r / 255f,
                    valueText = r.toString(),
                    brush = rBrush,
                    thumbColor = color,
                    onValueChange = { pushRgb(rr = (it * 255).roundToInt()) },
                )
                val gBrush = remember(r, b) {
                    Brush.horizontalGradient(listOf(rgbInts(r, 0, b), rgbInts(r, 255, b)))
                }
                LabeledSlider(
                    label = "G",
                    value = g / 255f,
                    valueText = g.toString(),
                    brush = gBrush,
                    thumbColor = color,
                    onValueChange = { pushRgb(gg = (it * 255).roundToInt()) },
                )
                val bBrush = remember(r, g) {
                    Brush.horizontalGradient(listOf(rgbInts(r, g, 0), rgbInts(r, g, 255)))
                }
                LabeledSlider(
                    label = "B",
                    value = b / 255f,
                    valueText = b.toString(),
                    brush = bBrush,
                    thumbColor = color,
                    onValueChange = { pushRgb(bb = (it * 255).roundToInt()) },
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("#", color = Color.White.copy(alpha = 0.6f), fontSize = 18.sp)
                TextField(
                    value = hexField,
                    onValueChange = { input ->
                        val cleaned = input.uppercase().filter { it.isDigit() || it in 'A'..'F' }.take(6)
                        hexField = cleaned
                        hexToColorOrNull(cleaned)?.let { setColor(it) }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }

        SectionLabel("Preset colors")
        SwatchGrid(colors = PRESET_COLORS) { setColor(it) }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 18.dp),
        ) {
            Text(
                "Saved colors",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
            )
            if (viewModel.savedColors.isNotEmpty()) {
                IconButton(onClick = { deleteMode = !deleteMode }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete saved colors",
                        tint = if (deleteMode) Color.White else Color.White.copy(alpha = 0.6f),
                    )
                }
            }
        }
        if (viewModel.savedColors.isEmpty()) {
            Text(
                "No saved colors yet",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 24.dp),
            )
        } else {
            SwatchGrid(
                colors = viewModel.savedColors,
                deleteMode = deleteMode,
                onDelete = { viewModel.deleteSavedColor(it) },
            ) { setColor(it) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModeToggle(mode: ColorMode, onModeChange: (ColorMode) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(4.dp),
    ) {
        ColorMode.entries.forEach { m ->
            val selected = m == mode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onModeChange(m) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = m.name,
                    color = if (selected) Color.Black else Color.White,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    valueText: String,
    brush: Brush,
    thumbColor: Color,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.width(20.dp))
        GradientSlider(
            value = value,
            onValueChange = onValueChange,
            trackBrush = brush,
            thumbColor = thumbColor,
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .padding(horizontal = 12.dp),
        )
        Text(
            valueText,
            color = Color.White,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 10.dp),
    )
}

@Composable
private fun SwatchGrid(
    colors: List<Color>,
    deleteMode: Boolean = false,
    onDelete: (Color) -> Unit = {},
    onSelect: (Color) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        colors.chunked(8).forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                rowColors.forEach { c ->
                    ColorSwatch(
                        color = c,
                        size = 30.dp,
                        showDeleteMark = deleteMode,
                        onClick = { if (deleteMode) onDelete(c) else onSelect(c) },
                    )
                }
            }
        }
    }
}
