package com.glyphlight.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphlight.HomeMode
import com.glyphlight.TorchViewModel
import com.glyphlight.ui.components.ColorSwatch
import com.glyphlight.ui.components.CustomColorSwatch
import com.glyphlight.ui.components.DotGridGlyph
import com.glyphlight.ui.components.DottedRing
import com.glyphlight.ui.components.DottedSlider
import com.glyphlight.ui.components.FLASHLIGHT_GRID
import com.glyphlight.ui.components.SOS_GRID
import com.glyphlight.ui.components.SleepTimerDialog
import com.glyphlight.ui.components.SunGlyph
import com.glyphlight.ui.components.ThreeDotMenu
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val QUICK_COLORS = listOf(
    0xFFFFFFFFL, 0xFFE8352BL, 0xFF34C759L, 0xFF2E7CF6L,
    0xFFFF9500L, 0xFF32D1C4L, 0xFFBF39F5L,
).map { Color(it) }

@Composable
fun HomeScreen(viewModel: TorchViewModel) {
    // Computing the torch-on row's top offset from window insets kept breaking on this device
    // (camera-cutout status bar height, immersive mode collapsing the inset to zero, etc).
    // Instead, measure exactly where Compose places the row while the idle screen is showing
    // (normal insets, nothing hidden) and reuse that literal pixel position for the torch-on
    // row — no inset math, no guessing, so it can't drift out of sync with reality.
    var brightnessRowTopPx by remember { mutableFloatStateOf(0f) }

    when (viewModel.homeMode) {
        HomeMode.Idle -> IdleHome(viewModel) { brightnessRowTopPx = it }
        HomeMode.Torch, HomeMode.Sos -> TorchOverlayScreen(viewModel, brightnessRowTopPx)
    }
}

@Composable
private fun IdleHome(viewModel: TorchViewModel, onBrightnessRowPositioned: (Float) -> Unit) {
    val accent = viewModel.selectedColor
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp),
    ) {
        BrightnessRow(
            viewModel,
            modifier = Modifier
                .padding(top = 16.dp)
                .onGloballyPositioned { onBrightnessRowPositioned(it.positionInRoot().y) },
        )

        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.openSleepTimerDialog() },
        ) {
            val remaining = viewModel.sleepTimerRemainingSeconds
            Icon(
                imageVector = Icons.Filled.Bedtime,
                contentDescription = "Sleep timer",
                tint = accent,
                modifier = Modifier.size(30.dp),
            )
            if (remaining != null) {
                Text(
                    text = formatRemaining(remaining),
                    color = accent,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { viewModel.cancelSleepTimerFromUser() },
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(200.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.toggleTorch() },
            contentAlignment = Alignment.Center,
        ) {
            DottedRing(color = accent, modifier = Modifier.fillMaxSize(), dotCount = 76)
            DotGridGlyph(grid = FLASHLIGHT_GRID, color = accent, modifier = Modifier.size(80.dp))
        }

        Spacer(Modifier.weight(1f))

        ThreeDotMenu(
            color = accent,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 52.dp, height = 24.dp),
            onClick = viewModel::openSettings,
        )

        Spacer(Modifier.weight(0.8f))

        DotGridGlyph(
            grid = SOS_GRID,
            color = accent,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 80.dp, height = 36.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.startSos() },
        )

        Spacer(Modifier.weight(0.8f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 36.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QUICK_COLORS.take(4).forEach { c ->
                    ColorSwatch(
                        color = c,
                        size = 20.dp,
                        selected = c == accent,
                        onClick = { viewModel.selectColor(c) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QUICK_COLORS.drop(4).forEach { c ->
                    ColorSwatch(
                        color = c,
                        size = 20.dp,
                        selected = c == accent,
                        onClick = { viewModel.selectColor(c) },
                    )
                }
                CustomColorSwatch(size = 20.dp, color = accent, onClick = viewModel::openColorPicker)
            }
        }
    }

    if (viewModel.showSleepTimerDialog) {
        SleepTimerDialog(
            initialMinutes = viewModel.sleepTimerMinutesSetting,
            accentColor = accent,
            onDismiss = viewModel::dismissSleepTimerDialog,
            onConfirm = viewModel::confirmSleepTimer,
        )
    }
}

@Composable
private fun TorchOverlayScreen(viewModel: TorchViewModel, brightnessRowTopPx: Float) {
    val isSos = viewModel.homeMode == HomeMode.Sos
    val accent = viewModel.selectedColor
    val fillColor = if (viewModel.isLightOn) accent else Color.Black
    var controlsVisible by remember { mutableStateOf(true) }

    LaunchedEffect(controlsVisible, isSos) {
        if (controlsVisible && !isSos) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fillColor)
            .pointerInput(isSos) {
                detectTapGestures {
                    if (isSos) viewModel.stopAll() else controlsVisible = true
                }
            },
    ) {
        // The brightness row keeps its exact measured position as the first child of this
        // column, so the sleep-timer readout below it never displaces the slider.
        val remainingSeconds = viewModel.sleepTimerRemainingSeconds
        if (!isSos && (viewModel.brightnessBarEnabled || remainingSeconds != null)) {
            AnimatedVisibility(
                visible = controlsVisible,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, brightnessRowTopPx.roundToInt()) },
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 28.dp),
                ) {
                    if (viewModel.brightnessBarEnabled) {
                        BrightnessRow(viewModel)
                    }
                    // Time left on the sleep timer, revealed by the same tap that reveals
                    // the controls. Tap the readout itself to cancel the timer.
                    if (remainingSeconds != null) {
                        Text(
                            text = formatRemaining(remainingSeconds),
                            color = accent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) { viewModel.cancelSleepTimerFromUser() },
                        )
                    }
                }
            }
        }
        if (isSos) {
            Text(
                text = "SOS ACTIVE  ·  TAP TO STOP",
                color = accent,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 24.dp),
            )
        }
        AnimatedVisibility(
            visible = controlsVisible || isSos,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BottomActionBar(
                label = if (isSos) "STOP" else "OFF",
                color = accent,
                onClick = viewModel::stopAll,
            )
        }
    }
}

/** The brightness slider row: identical composable used on both the idle screen and the
 *  torch-on overlay, so toggling the torch never changes its position or appearance. */
@Composable
private fun BrightnessRow(viewModel: TorchViewModel, modifier: Modifier = Modifier) {
    val accent = viewModel.selectedColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        SunGlyph(
            color = accent,
            modifier = Modifier
                .size(18.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.updateBrightness(0.1f) },
        )
        DottedSlider(
            value = viewModel.brightness,
            onValueChange = viewModel::updateBrightness,
            activeColor = accent,
            inactiveColor = accent.copy(alpha = 0.25f),
            thumbColor = accent,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .padding(horizontal = 16.dp),
        )
        SunGlyph(
            color = accent,
            modifier = Modifier
                .size(30.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { viewModel.updateBrightness(1f) },
        )
    }
}

@Composable
private fun BottomActionBar(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.Black.copy(alpha = 0.30f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
        )
    }
}

private fun formatRemaining(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
