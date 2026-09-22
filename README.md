# StudioDroid v2

StudioDroid is a new Android ARM64 launcher/runtime project intended to run the real,
unmodified Roblox Studio Windows x86_64 executable. It starts from a clean architecture,
without the previous RobloxDroid internals.

## Current status

**M1: Android launcher foundation.** Kotlin/Material 3 navigation, real Android capability
observations, conservative memory policies, bounded logs, settings and diagnostic export.
The three modules are `:app`, `:core`, and `:runtime:android`.

Runtime execution, containers, Wine, graphics translation/drivers, Studio installation and
NativeSurfaceBackend implementation are unavailable. No Studio compatibility or physical
4 GB qualification is claimed. M2 requires separate approval.

## Architecture

- Kotlin and Material 3 control plane; runtime-independent interfaces.
- ARM64 glibc host environment, FEX initially, x86_64 guest environment, then Wine/DXVK.
- Versioned components, streaming installation, bounded logs, and recoverable updates.
- LOW_MEMORY, BALANCED, and PERFORMANCE profiles; 4 GB RAM is a first-class target.
- StudioDroid-owned `PresentationBackend` → `NativeSurfaceBackend`. Wine protocol
  compatibility is separate from Android presentation. The internal transport remains
  open until M6 qualification; no Termux:X11 runtime/app or external X server dependency.
- Signed sideloaded APKs with a modern target SDK; no root or SELinux changes required.

Read [Architecture](docs/ARCHITECTURE.md), [Roadmap](docs/ROADMAP.md),
[Acceptance](docs/ACCEPTANCE.md), [Build and CI](docs/BUILD_AND_CI.md), and
[Research references](docs/REFERENCES.md). The [decision records](docs/decisions/0001-platform-boundaries.md)
distinguish architectural commitments from unverified mechanisms.

## Building and validation

Use JDK 17 and Android SDK 36 on a supported build host:

```sh
./gradlew :core:test :runtime:android:testDebugUnitTest :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`. Android CI also uploads it
with a SHA-256 checksum. See [M1 validation](docs/M1_VALIDATION.md) for actual results,
local-host limitations and instrumentation commands. Independent pipelines are described
in [Build and CI](docs/BUILD_AND_CI.md).

The four files in `schemas/` use JSON Schema Draft 2020-12. Validate their schemas with
a conforming validator, check local Markdown links and `git diff --check`, and enforce
the research-only metadata invariants in [Payload format](docs/PAYLOAD_FORMAT.md).
Run the permanent M0.1 contract suite using the [reproducible validation commands](docs/ACCEPTANCE.md#m01-reproducible-validation).
Physical-device and runtime tests are explicitly deferred to their milestones.

## License and upstream software

StudioDroid-owned work is GPL-3.0-only; see [LICENSE](LICENSE) and [NOTICE](NOTICE).
Upstream components retain their own licenses. Roblox binaries, assets, credentials,
and signing keys are not included. Studio will be downloaded from legitimate Roblox
sources at runtime. StudioDroid is not affiliated with or endorsed by Roblox Corporation.
