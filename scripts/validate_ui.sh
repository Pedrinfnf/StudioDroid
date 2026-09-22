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
adb shell wm size 1600x2560
./gradlew --no-daemon :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.studiodroid.app.LauncherVisualTest \
    -Pandroid.testInstrumentationRunnerArguments.visualVariant=tablet
adb shell wm size reset
adb pull /sdcard/Download/studiodroid-qa app/build/reports/ui/captures
python3 - <<'CHECK_CAPTURES'
from pathlib import Path
names = {p.name for p in Path('app/build/reports/ui/captures').rglob('*.png') if p.stat().st_size > 1024}
for variant in ('phone', 'large-font', 'landscape', 'tablet'):
    for page in ('home', 'runtime', 'diagnostics', 'storage', 'logs', 'settings', 'about'):
        assert f'{variant}-{page}.png' in names, (variant, page)
        assert f'{variant}-{page}-bottom.png' in names, (variant, page, 'bottom')
    assert f'{variant}-drawer.png' in names, variant
assert 'phone-logs-raw.png' in names
print('Verified 60 screen captures and the raw-log dialog')
CHECK_CAPTURES
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -W -n org.studiodroid.app/.MainActivity
adb shell input keyevent KEYCODE_HOME
adb shell am start -W -n org.studiodroid.app/.MainActivity
adb exec-out screencap -p > app/build/reports/ui/launcher.png
adb shell dumpsys meminfo org.studiodroid.app > app/build/reports/ui/memory.txt

# Optional controlled comparison against an existing debug artifact from this repository.
# Fresh installs and the same emulator/settings are used for both launcher versions.
if [ -n "${STUDIODROID_BASELINE_RUN:-}" ]; then
    case "$STUDIODROID_BASELINE_RUN" in *[!0-9]*) exit 2 ;; esac
    gh run download "$STUDIODROID_BASELINE_RUN" --pattern 'StudioDroid-M1-debug-*' --dir artifacts/ui-baseline
    sleep 5
    adb shell dumpsys meminfo org.studiodroid.app > app/build/reports/ui/memory-new-idle.txt
    adb uninstall org.studiodroid.app
    baseline_apk=$(find artifacts/ui-baseline -name app-debug.apk -type f -print -quit)
    test -n "$baseline_apk"
    adb install "$baseline_apk"
    adb shell am start -W -n org.studiodroid.app/.MainActivity
    sleep 5
    adb shell dumpsys meminfo org.studiodroid.app > app/build/reports/ui/memory-baseline-idle.txt
    adb uninstall org.studiodroid.app
    adb install app/build/outputs/apk/debug/app-debug.apk
fi
