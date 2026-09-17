package com.glyphlight

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glyphlight.util.MorseSos
import com.glyphlight.util.PreferencesRepository
import com.glyphlight.util.SoundEffects
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen { Home, Settings, ColorPicker }
enum class HomeMode { Idle, Torch, Sos }

class TorchViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesRepository(application)
    private val sound = SoundEffects()

    var screen by mutableStateOf(Screen.Home)
        private set

    var homeMode by mutableStateOf(HomeMode.Idle)
        private set

    val isOverlayActive: Boolean get() = homeMode != HomeMode.Idle

    /** Whether the screen should currently be emitting light (steady for torch, strobing for SOS). */
    var isLightOn by mutableStateOf(false)
        private set

    var selectedColor by mutableStateOf(Color(prefs.selectedColorArgb))
        private set

    var editingColor by mutableStateOf(selectedColor)
        private set

    var brightness by mutableFloatStateOf(prefs.brightness)
        private set

    var savedColors by mutableStateOf(prefs.savedColors.map { Color(it) })
        private set

    var brightnessBarEnabled by mutableStateOf(prefs.brightnessBarEnabled)
        private set
    var fullBrightnessControl by mutableStateOf(prefs.fullBrightnessControl)
        private set
    var preventMainScreenLock by mutableStateOf(prefs.preventMainScreenLock)
        private set
    var preventColorPickerScreenLock by mutableStateOf(prefs.preventColorPickerScreenLock)
        private set
    var soundEffectsEnabled by mutableStateOf(prefs.soundEffectsEnabled)
        private set

    var sleepTimerMinutesSetting by mutableIntStateOf(prefs.sleepTimerMinutes)
        private set
    var sleepTimerRemainingSeconds by mutableStateOf<Int?>(null)
        private set
    var showSleepTimerDialog by mutableStateOf(false)
        private set

    private var sosJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun click() {
        if (soundEffectsEnabled) sound.click()
    }

    fun toggleTorch() {
        click()
        if (homeMode == HomeMode.Torch) {
            stopAll()
        } else {
            sosJob?.cancel()
            homeMode = HomeMode.Torch
            isLightOn = true
        }
    }

    fun startSos() {
        click()
        if (homeMode == HomeMode.Sos) {
            stopAll()
            return
        }
        homeMode = HomeMode.Sos
        sosJob?.cancel()
        sosJob = viewModelScope.launch {
            while (isActive) {
                for ((on, duration) in MorseSos.sequence) {
                    isLightOn = on
                    delay(duration)
                }
            }
        }
    }

    fun stopAll() {
        sosJob?.cancel()
        sosJob = null
        homeMode = HomeMode.Idle
        isLightOn = false
    }

    fun updateBrightness(value: Float) {
        brightness = value.coerceIn(0.03f, 1f)
        prefs.brightness = brightness
    }

    fun selectColor(color: Color) {
        click()
        selectedColor = color
        prefs.selectedColorArgb = color.toArgb()
    }

    fun openColorPicker() {
        click()
        editingColor = selectedColor
        screen = Screen.ColorPicker
    }

    fun updateEditingColor(color: Color) {
        editingColor = color
    }

    fun applyEditingColor() {
        click()
        selectColor(editingColor)
        screen = Screen.Home
    }

    fun saveEditingColorToPalette() {
        click()
        val argb = editingColor.toArgb()
        val current = savedColors.map { it.toArgb() }.toMutableList()
        current.remove(argb)
        current.add(0, argb)
        val trimmed = current.take(16)
        savedColors = trimmed.map { Color(it) }
        prefs.savedColors = trimmed
    }

    fun deleteSavedColor(color: Color) {
        click()
        val argb = color.toArgb()
        val updated = savedColors.filter { it.toArgb() != argb }
        savedColors = updated
        prefs.savedColors = updated.map { it.toArgb() }
    }

    fun openSettings() {
        click()
        screen = Screen.Settings
    }

    fun closeToHome() {
        screen = Screen.Home
    }

    fun updateBrightnessBarEnabled(value: Boolean) {
        brightnessBarEnabled = value
        prefs.brightnessBarEnabled = value
    }

    fun updateFullBrightnessControl(value: Boolean) {
        fullBrightnessControl = value
        prefs.fullBrightnessControl = value
    }

    fun updatePreventMainScreenLock(value: Boolean) {
        preventMainScreenLock = value
        prefs.preventMainScreenLock = value
    }

    fun updatePreventColorPickerScreenLock(value: Boolean) {
        preventColorPickerScreenLock = value
        prefs.preventColorPickerScreenLock = value
    }

    fun updateSoundEffectsEnabled(value: Boolean) {
        soundEffectsEnabled = value
        prefs.soundEffectsEnabled = value
    }

    fun openSleepTimerDialog() {
        click()
        showSleepTimerDialog = true
    }

    fun dismissSleepTimerDialog() {
        showSleepTimerDialog = false
    }

    fun confirmSleepTimer(minutes: Int) {
        click()
        sleepTimerMinutesSetting = minutes
        prefs.sleepTimerMinutes = minutes
        showSleepTimerDialog = false
        armSleepTimer(minutes)
    }

    /**
     * Counts down against a wall-clock deadline rather than accumulating 1s delays, so the
     * displayed time cannot drift away from the real remaining time if the coroutine is
     * throttled while the app sits in the background.
     */
    private fun armSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val deadlineMs = System.currentTimeMillis() + minutes * 60_000L
        sleepTimerRemainingSeconds = minutes * 60
        sleepTimerJob = viewModelScope.launch {
            while (isActive) {
                val msLeft = deadlineMs - System.currentTimeMillis()
                if (msLeft <= 0) {
                    sleepTimerRemainingSeconds = null
                    onSleepTimerExpired()
                    break
                }
                // Round up so a 5:00 timer reads "5:00" rather than "4:59" on the first frame.
                sleepTimerRemainingSeconds = ((msLeft + 999) / 1000).toInt()
                delay(msLeft.coerceAtMost(1000L))
            }
        }
    }

    /**
     * The timer elapsed: kill any active light and let the display fall asleep.
     *
     * Android gives an ordinary app no way to lock the screen outright (that needs device
     * admin or an accessibility service), so instead this drops every reason the app had to
     * hold the display awake. FLAG_KEEP_SCREEN_ON is derived from "overlay active" or
     * "color picker open", so returning to an idle Home clears it and the display sleeps on
     * the normal system timeout.
     *
     * Deliberately does not route through [stopAll] plus [cancelSleepTimer] - the job is
     * already finishing, so cancelling it from inside itself is needless.
     */
    private fun onSleepTimerExpired() {
        sosJob?.cancel()
        sosJob = null
        sleepTimerJob = null
        homeMode = HomeMode.Idle
        isLightOn = false
        screen = Screen.Home
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepTimerRemainingSeconds = null
    }

    /**
     * User-initiated cancel: tapping the countdown readout. This is the only way to stop an
     * armed timer short of its deadline - turning the light off no longer cancels it, so a
     * timer always fires once set.
     */
    fun cancelSleepTimerFromUser() {
        click()
        cancelSleepTimer()
    }

    override fun onCleared() {
        sosJob?.cancel()
        sleepTimerJob?.cancel()
        sound.release()
        super.onCleared()
    }
}
