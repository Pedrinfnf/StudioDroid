# Build and CI architecture

Status: M1 application, Gradle and CI foundation implemented. Later pipeline rows remain
planned; M2 is not started. See [M1 validation](M1_VALIDATION.md).

## Initial application baseline (M1)

Kotlin, Material 3 Views, ViewModels, StateFlow and lifecycle-aware collection. Modules:
`:app`, `:core`, `:runtime:android`. Android minSdk 29, compileSdk 36, targetSdk 36,
arm64-v8a only. JDK 17, Gradle 8.13, AGP 8.13.2; NDK r28 with 16 KiB-compatible packaging.
M1 pins Kotlin 2.2.20, coroutines 1.10.2, AndroidX and Material versions in the version
catalog, Build Tools 36.0.0, and NDK r28c 28.2.13676358. The Gradle distribution and wrapper
JAR have verified upstream SHA-256 values. CI actions are pinned to commit SHAs. Full
independent repeat-build reproducibility is not established by these version pins.

[AGP compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
documents the Gradle/JDK pair. [Android page sizes](https://developer.android.com/guide/practices/page-sizes)
require packaging and actual native compatibility checks; alignment alone does not prove
FEX guest operation. Keep executable bootstrap handling behind the runtime interface.

## Independent pipelines

| Pipeline | Introduced | Checks/output |
| --- | --- | --- |
| Android | M1 | Unit/lint/assembly, manifest exports/permissions, APK ABI and signature verification |
| Native/JNI | M1 skeleton; M2+ implementations | Android compilation, host tests, sanitizers and archive/protocol fuzzing |
| Payloads | M2 | Inventory/schema/digest/security/transaction checks; independently verified artifacts |
| ARM64 rootfs | M3 | Minimal Ubuntu 24.04 package closure, pinned snapshot, normalized archive and licenses |
| Proot/bootstrap | M3 | Source-built Android binaries, pinned dependencies, ELF loader/ABI audit, APK-installed storage |
| Device qualification | M3 | Physical app-UID execution and structured bounded diagnostics |
| FEX / x86_64 rootfs | M4 | Pinned FEX/submodules and minimal glibc guest, owned static/dynamic fixtures and ELF audits |
| Wine | M5+ | Independent Wine build/prefix qualification |
| DXVK | M7+ | Version requirements, DLL architecture, graphics qualification |
| Graphics driver packages | M15+ | Compatibility/signatures/inventory/license/probe validation |

Path/input hashes select jobs. UI-only changes do not rebuild rootfs/FEX. Cache only
verified immutable artifacts; independently recompute trusted keys. Do not share mutable
staging directories across component jobs. APK/signed runtime delivery must preserve
component identity even where an executable update requires a signed APK carrier update.

## Reproducibility and source obligations

Resolve source pins, recursive submodules, build containers by digest, package snapshot
URLs/versions/hashes and toolchain revisions. Authenticate upstream package metadata.
Use deterministic file order, ownership, modes, timestamps/SOURCE_DATE_EPOCH and compression.
Build twice from independent clean directories and compare unsigned content hashes;
report any nondeterminism rather than claiming reproducibility from a checksum alone.

The M0 [source inventory](../runtime/manifests/sources.lock.json) is deliberately
non-buildable. A fully locked reviewed input set is mandatory before publishing artifacts.
No branch HEAD, mutable latest download, arbitrary mirror or unverified binary patch.
Separate debug symbols, build provenance, SBOM and corresponding source from stripped
release payloads. Preserve package-specific licenses; see [NOTICE](../NOTICE).

Release signing/publication is separate from PR checks. Keep private keys out of source
and untrusted CI jobs; verify artifacts before signing. Signed sideloaded APKs are the
initial distribution. Future Play Store qualification must not weaken modern execution
or scoped-storage boundaries. No Roblox binaries in source, APK or CI artifacts.

## Implementation boundaries

M1 adds application/build/contracts/capability/logging foundation. M2 adds payload manager,
SQLite records, hardened archive integration and package verification. M3 adds protocol,
container/supervisor/host probing and host environment packaging. M4 adds FexBackend,
config writer/validator, guest environment and execution proof. Exact source names and
boundaries remain those in the approved M0–M4 plan; no new implementation tranche is
implied by this document. Keep the first tranche free of Wine/Studio/driver/audio loaders.

The four existing `.gitkeep` files remain. Generated rootfs/test ELF/native libraries,
reports, signatures and SBOMs belong in build/artifact outputs, not source. `.gitignore`
permits a future Gradle wrapper JAR while continuing to exclude generated JARs, payloads
and local signing material. Schema tests are M0 validation, not an installer implementation.

## M1 workflow scope

`android.yml` validates M0, Gradle configuration, core/runtime/app unit tests, Android lint,
debug APK assembly and instrumentation APK compilation. It verifies the debug APK signature
and uploads APK/checksum/reports. Instrumentation execution requires an Android device;
compiling its APK is not a passing lifecycle test.

`native.yml` compiles a JNI type/ABI boundary against the pinned Android ARM64 NDK and
CMake 3.22.1. This compile-only static library is neither packaged nor loaded by M1.
NativeBridge reports Unimplemented; no native executable, container or graphics function
is provided. App ABI filters and controller checks target ARM64; the Java-only foundation
APK itself has no native ABI payload and may install elsewhere, where execution remains
unsupported. No M2 payload or runtime build workflow is introduced.
