package com.glyphlight.util

/** Timing sequence for the international SOS distress signal, looped by the caller. */
object MorseSos {
    private const val UNIT_MS = 200L
    private const val DOT = UNIT_MS
    private const val DASH = UNIT_MS * 3
    private const val GAP_SYMBOL = UNIT_MS
    private const val GAP_LETTER = UNIT_MS * 3
    private const val GAP_WORD = UNIT_MS * 7

    /** List of (lightOn, durationMs) steps. Play in order, then repeat from the top. */
    val sequence: List<Pair<Boolean, Long>> = buildList {
        fun letter(vararg symbols: Long) {
            symbols.forEachIndexed { index, duration ->
                add(true to duration)
                if (index != symbols.lastIndex) add(false to GAP_SYMBOL)
            }
        }
        letter(DOT, DOT, DOT)
        add(false to GAP_LETTER)
        letter(DASH, DASH, DASH)
        add(false to GAP_LETTER)
        letter(DOT, DOT, DOT)
        add(false to GAP_WORD)
    }
}
