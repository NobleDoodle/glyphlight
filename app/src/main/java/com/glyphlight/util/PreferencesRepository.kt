package com.glyphlight.util

import android.content.Context
import android.content.SharedPreferences

/** Thin wrapper over SharedPreferences for the small amount of state this app persists. */
class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var selectedColorArgb: Int
        get() = prefs.getInt(KEY_COLOR, DEFAULT_COLOR)
        set(value) = prefs.edit().putInt(KEY_COLOR, value).apply()

    var brightness: Float
        get() = prefs.getFloat(KEY_BRIGHTNESS, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_BRIGHTNESS, value).apply()

    var sleepTimerMinutes: Int
        get() = prefs.getInt(KEY_SLEEP_MINUTES, 10)
        set(value) = prefs.edit().putInt(KEY_SLEEP_MINUTES, value).apply()

    var brightnessBarEnabled: Boolean
        get() = prefs.getBoolean(KEY_BRIGHTNESS_BAR, true)
        set(value) = prefs.edit().putBoolean(KEY_BRIGHTNESS_BAR, value).apply()

    var fullBrightnessControl: Boolean
        get() = prefs.getBoolean(KEY_FULL_BRIGHTNESS, false)
        set(value) = prefs.edit().putBoolean(KEY_FULL_BRIGHTNESS, value).apply()

    var preventMainScreenLock: Boolean
        get() = prefs.getBoolean(KEY_PREVENT_MAIN_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_PREVENT_MAIN_LOCK, value).apply()

    var preventColorPickerScreenLock: Boolean
        get() = prefs.getBoolean(KEY_PREVENT_PICKER_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_PREVENT_PICKER_LOCK, value).apply()


    var savedColors: List<Int>
        get() = prefs.getString(KEY_SAVED_COLORS, "")
            .orEmpty()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
        set(value) = prefs.edit().putString(KEY_SAVED_COLORS, value.joinToString(",")).apply()

    companion object {
        private const val PREFS_NAME = "glyphlight_prefs"
        private const val KEY_COLOR = "selected_color"
        private const val KEY_BRIGHTNESS = "brightness"
        private const val KEY_SLEEP_MINUTES = "sleep_minutes"
        private const val KEY_BRIGHTNESS_BAR = "brightness_bar_enabled"
        private const val KEY_FULL_BRIGHTNESS = "full_brightness_control"
        private const val KEY_PREVENT_MAIN_LOCK = "prevent_main_lock"
        private const val KEY_PREVENT_PICKER_LOCK = "prevent_picker_lock"
        private const val KEY_SAVED_COLORS = "saved_colors"

        /** A warm accent red, close to the reference mockups. */
        val DEFAULT_COLOR: Int = 0xFFE8352B.toInt()
    }
}
