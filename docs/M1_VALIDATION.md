# M1 implementation and validation

M1 only. M2 is not started. The accidental `tatus --short` file was already absent;
baseline commit `ba8f1a5` records its removal.

## Implemented

- Three modules, approved Android SDK/JDK/Gradle/AGP baseline, Material 3 Views and drawer
  navigation: Home, Runtime, Diagnostics, Storage, Logs, Settings and About.
- Generic component/capability/launch/session/graphics contracts and pure launch preflight.
  No executable backend or container is registered. NativeSurfaceBackend ownership and
  the open M6 transport decision remain unchanged.
- App-scoped controller, idempotent lifecycle clients, dormant non-exported bound service,
  settings, atomic bounded session metadata and unknown-outcome recovery.
- Real public Android observations: ABI/process width, OS/model, SoC where available,
  page size, physical/available RAM, Android low-RAM/memory pressure, own-process PSS,
  thermal status, storage, and PackageManager-advertised Vulkan version. Swap is optional,
  bounded `/proc/meminfo` observation. GPU model/extensions/driver remain explicitly unknown.
- No custom native/GL probe is run. Advertising Vulkan does not prove driver compatibility.
- Memory monitoring runs only while UI clients are visible, normally every five seconds
  (one second under pressure); PSS/swap are sampled less often. Log writes have no RAM queue,
  four rotated disk files, bounded UI tails and conservative pressure-adjusted budgets.
- SAF diagnostic export contains bounded generated metadata and at most 128 KiB of typed
  launcher logs. No project/account content, arbitrary command, broad storage permission,
  background runtime service, root assumption or largeHeap is introduced.

## Reproduce on a supported x86_64 build host

Install JDK 17 and Android SDK packages `platforms;android-36` and `build-tools;36.0.0`.
Set ANDROID_HOME or an ignored local.properties SDK path, then run:

```sh
./gradlew help
./gradlew :core:test :runtime:android:testDebugUnitTest :app:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

For lifecycle instrumentation, connect an Android 29+ device and run:

```sh
./gradlew :app:connectedDebugAndroidTest
```

M0/M0.1 checks remain unchanged; use [their reproducible command](ACCEPTANCE.md#m01-reproducible-validation).
The Android and native CI workflows provide separate build-host checks. APK signing is
Android's debug signing, not a production release or runtime compatibility certification.

## Evidence and limitations

Local host: Android ARM64 Termux; OpenJDK 17.0.20, Gradle 8.13. Official SDK 36 and Build
Tools 36.0.0 archives were verified against Google's repository metadata. Gradle wrapper
and distribution SHA-256 values were checked against upstream.

M0/M0.1: four schemas, 416 fixtures, four bases, nine parser cases, 49 ECMAScript patterns
and 87 assertions passed. Local Gradle configuration passed. All 20 JVM tests passed: DeviceProfilePolicy (6),
MemoryPolicy (4), LaunchPlanner (8), and RotatingLogStore (2). Runtime Android Kotlin
compilation also passed. No result is inferred from source inspection.

The default Maven Central hostname initially failed DNS. Local-only Gradle init tooling
uses Maven Central's official repo1 endpoint; repository build configuration is unchanged.
Local Gradle native/instrumentation integration was disabled only for the Termux host JVM;
no Android target, runtime architecture or tool version was downgraded.

Pending: final lint/APK/native CI and emulator lifecycle results, plus physical-device
UI and memory measurements. The CI emulator is x86_64 and cannot qualify ARM64 runtime
compatibility; it tests only launcher UI lifecycle. The 4 GB memory budgets are design targets, not measured qualification.
