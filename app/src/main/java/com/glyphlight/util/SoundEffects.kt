package com.glyphlight.util

import android.media.AudioManager
import android.media.ToneGenerator

/** Tiny UI click/blip player used when the "Sound effects" setting is enabled. */
class SoundEffects {
    private var toneGenerator: ToneGenerator? = null

    private fun generator(): ToneGenerator =
        toneGenerator ?: ToneGenerator(AudioManager.STREAM_SYSTEM, 60).also { toneGenerator = it }

    fun click() {
        runCatching { generator().startTone(ToneGenerator.TONE_PROP_BEEP, 40) }
    }

    fun release() {
        runCatching { toneGenerator?.release() }
        toneGenerator = null
    }
}
