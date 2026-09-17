# Glyphlight

A minimal screen flashlight for Android. The whole UI is drawn as
dot-matrix glyphs on a pure black (OLED-friendly) background, with every accent
element following one user-selected color.

## Features

- **Big torch button** — dotted ring + dot-matrix flashlight glyph. Tap to fill
  the screen with the selected color at the chosen brightness.
- **Brightness slider** — dotted, top of screen; sets the real screen backlight
  brightness (via the window's `screenBrightness` attribute), not just an overlay.
- **Sleep timer** — tap the moon glyph, pick a duration, and the torch/SOS auto-
  turns off when it elapses. Tap the countdown badge to cancel early.
- **SOS** — dot-matrix "SOS" label strobes the selected color in real Morse
  timing (`... --- ...`) until tapped again.
- **Color picker** — three-dot menu-adjacent quick swatches, plus a full
  HSL/RGB picker with hex entry, presets, and a saved-colors palette (with
  delete mode).
- **Settings** — brightness bar visibility, full-app vs. torch-only brightness
  control, screen-lock prevention (main + color picker), and a sound-effects
  toggle (short UI blips via `ToneGenerator`).

No permissions are required — the "flashlight" is the screen itself, not the
camera LED.

## Install

Download `glyphlight.apk` from the [latest release](https://github.com/NobleDoodle/glyphlight/releases/latest)
and sideload it (`adb install glyphlight.apk`, or open the file on-device and allow
installation from unknown sources).

The released APK is signed with the standard Android debug certificate, so Android
may warn about the installation source. A production build signed with a private
release keystore is not yet published.

## Building

This is a standard Gradle/Kotlin/Jetpack Compose Android Studio project.

1. Open the `glyphlight/` folder in Android Studio (Iguana/Koala or newer).
2. Let it sync — it targets Kotlin 2.0.0, AGP 8.4.0, compile/target SDK 34,
   min SDK 24, Java 17 toolchain (bundled with recent Android Studio).
3. Run on a device or emulator.

From the command line, once the Gradle wrapper has downloaded its distribution:

```sh
./gradlew assembleDebug
```

Verified building and running on a physical device via `./gradlew assembleDebug` + `adb install`.

## Project layout

```
app/src/main/java/com/glyphlight/
  MainActivity.kt          – entry point
  GlyphlightApp.kt          – screen switch, back handling, brightness/wake-lock effects
  TorchViewModel.kt         – all app state (torch/SOS/settings/colors)
  ui/components/            – dot-matrix primitives (ring, slider, glyphs, swatches)
  ui/screens/                – Home (idle + torch/SOS overlay), Settings, Color Picker
  util/                     – color math, Morse timing, sound, SharedPreferences
```
