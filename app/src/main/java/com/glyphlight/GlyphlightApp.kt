package com.glyphlight

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glyphlight.ui.screens.ColorPickerScreen
import com.glyphlight.ui.screens.HomeScreen
import com.glyphlight.ui.screens.SettingsScreen
import com.glyphlight.ui.theme.GlyphlightTheme

@Composable
fun GlyphlightApp(viewModel: TorchViewModel = viewModel()) {
    val activity = LocalContext.current as Activity

    DisposableEffect(
        viewModel.isOverlayActive,
        viewModel.preventMainScreenLock,
        viewModel.screen,
        viewModel.preventColorPickerScreenLock,
    ) {
        val shouldKeepOn = (viewModel.isOverlayActive && viewModel.preventMainScreenLock) ||
            (viewModel.screen == Screen.ColorPicker && viewModel.preventColorPickerScreenLock)
        if (shouldKeepOn) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(viewModel.isOverlayActive) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (viewModel.isOverlayActive) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    LaunchedEffect(
        viewModel.isOverlayActive,
        viewModel.brightnessBarEnabled,
        viewModel.fullBrightnessControl,
        viewModel.brightness,
    ) {
        val shouldOverride = (viewModel.isOverlayActive && viewModel.brightnessBarEnabled) ||
            viewModel.fullBrightnessControl
        val params = activity.window.attributes
        params.screenBrightness = if (shouldOverride) {
            viewModel.brightness
        } else {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        activity.window.attributes = params
    }

    BackHandler(enabled = viewModel.isOverlayActive) { viewModel.stopAll() }
    BackHandler(enabled = !viewModel.isOverlayActive && viewModel.screen != Screen.Home) {
        viewModel.closeToHome()
    }

    GlyphlightTheme(accentColor = viewModel.selectedColor) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (viewModel.screen) {
                Screen.Home -> HomeScreen(viewModel)
                Screen.Settings -> SettingsScreen(viewModel)
                Screen.ColorPicker -> ColorPickerScreen(viewModel)
            }
        }
    }
}
