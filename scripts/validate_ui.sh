#!/usr/bin/env sh
# SPDX-License-Identifier: GPL-3.0-only
# Run only on a disposable connected QA emulator. Never use this on a personal device:
# it changes emulated dimensions/font scale and resets those emulator settings on exit.
set -eu
mkdir -p app/build/reports/ui
finish() {
    adb pull /sdcard/Download/studiodroid-qa app/build/reports/ui/captures || true
    adb shell settings put system font_scale 1.0 || true
    adb shell wm size reset || true
}
trap finish EXIT
./gradlew --no-daemon :app:connectedDebugAndroidTest
adb shell wm size 900x1600
adb shell settings put system font_scale 1.6
./gradlew --no-daemon :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.studiodroid.app.LauncherVisualTest \
    -Pandroid.testInstrumentationRunnerArguments.visualVariant=large-font
adb shell settings put system font_scale 1.0
adb shell wm size 1920x1080
./gradlew --no-daemon :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.studiodroid.app.LauncherVisualTest \
    -Pandroid.testInstrumentationRunnerArguments.visualVariant=landscape
adb shell wm size reset
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n org.studiodroid.app/.MainActivity
adb shell input keyevent KEYCODE_HOME
adb shell am start -W -n org.studiodroid.app/.MainActivity
adb exec-out screencap -p > app/build/reports/ui/launcher.png
adb shell dumpsys meminfo org.studiodroid.app > app/build/reports/ui/memory.txt
