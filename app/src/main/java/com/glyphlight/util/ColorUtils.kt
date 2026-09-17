package com.glyphlight.util

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/** h in degrees [0,360), s and l in [0,1]. */
fun hslToColor(hDeg: Float, s: Float, l: Float): Color {
    val h = (((hDeg % 360f) + 360f) % 360f) / 360f
    if (s <= 0f) return Color(l, l, l)
    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q
    val r = hueToRgb(p, q, h + 1f / 3f)
    val g = hueToRgb(p, q, h)
    val b = hueToRgb(p, q, h - 1f / 3f)
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}

private fun hueToRgb(p: Float, q: Float, tIn: Float): Float {
    var t = tIn
    if (t < 0f) t += 1f
    if (t > 1f) t -= 1f
    return when {
        t < 1f / 6f -> p + (q - p) * 6f * t
        t < 1f / 2f -> q
        t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
        else -> p
    }
}

/** Returns [hueDegrees, saturation, lightness]. */
fun colorToHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f
    if (max == min) return floatArrayOf(0f, 0f, l)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    var h = when (max) {
        r -> (g - b) / d + (if (g < b) 6f else 0f)
        g -> (b - r) / d + 2f
        else -> (r - g) / d + 4f
    }
    h *= 60f
    return floatArrayOf(h, s, l)
}

fun colorToHex(color: Color): String {
    val r = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val g = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val b = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format("%02X%02X%02X", r, g, b)
}

fun hexToColorOrNull(hexIn: String): Color? {
    val hex = hexIn.trim().removePrefix("#")
    if (hex.length != 6 || !hex.matches(Regex("^[0-9a-fA-F]{6}$"))) return null
    return try {
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        Color(r / 255f, g / 255f, b / 255f)
    } catch (e: NumberFormatException) {
        null
    }
}

fun Color.redInt(): Int = (red * 255f).roundToInt().coerceIn(0, 255)
fun Color.greenInt(): Int = (green * 255f).roundToInt().coerceIn(0, 255)
fun Color.blueInt(): Int = (blue * 255f).roundToInt().coerceIn(0, 255)

fun rgbInts(r: Int, g: Int, b: Int): Color =
    Color(r.coerceIn(0, 255) / 255f, g.coerceIn(0, 255) / 255f, b.coerceIn(0, 255) / 255f)
