# StudioDroid v2

StudioDroid is a new Android ARM64 launcher/runtime project intended to run the real,
unmodified Roblox Studio Windows x86_64 executable. It starts from a clean architecture,
without the previous RobloxDroid internals.

## Current status

**M0: architecture, research records, metadata schemas, and acceptance policy.**
There is no Android application, Gradle build, JNI implementation, installed runtime,
or demonstrated Studio execution in this repository yet. Schemas describe contracts;
they do not implement validation or establish runtime compatibility.

M1 requires separate approval. The approved first implementation tranche ends at M4:
verified FEX execution of x86_64 Linux programs in the installed APK's own context on
physical Android ARM64 hardware, including a 4 GB device. Wine and Studio follow later.

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

M0 has no APK/build command. Do not interpret empty source directories as implemented
modules. Future build tools and independent runtime pipelines are specified in
[Build and CI](docs/BUILD_AND_CI.md).

The four files in `schemas/` use JSON Schema Draft 2020-12. Validate their schemas with
a conforming validator, check local Markdown links and `git diff --check`, and enforce
the research-only metadata invariants in [Payload format](docs/PAYLOAD_FORMAT.md).
Physical-device and runtime tests are explicitly deferred to their milestones.

## License and upstream software

StudioDroid-owned work is GPL-3.0-only; see [LICENSE](LICENSE) and [NOTICE](NOTICE).
Upstream components retain their own licenses. Roblox binaries, assets, credentials,
and signing keys are not included. Studio will be downloaded from legitimate Roblox
sources at runtime. StudioDroid is not affiliated with or endorsed by Roblox Corporation.
