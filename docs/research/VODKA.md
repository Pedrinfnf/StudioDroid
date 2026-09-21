# Vodka architecture review

Inspected [ca41f2b074fb82a5dc203411c234ebf18b455d9c](https://github.com/Shellworks-Development/vodka/tree/ca41f2b074fb82a5dc203411c234ebf18b455d9c).
This is source evidence, not physical-device validation. No runtime source was copied.

## Coverage

Reviewed README.md, docs/ARCHITECTURE.md, docs/M2-FEX-WINE.md, decision records,
native/container, native/archive, native/runtime, native/tests, runtime/fex,
runtime/rootfs-arm64, runtime/rootfs-x86_64, app Kotlin/resources/manifest/Gradle,
all three JNI bridges, .github/workflows, tools/device-probe, tools/vulkan-probe and
proot-kit packaging. Docs are aspirational and sometimes stale; source takes precedence.

## Actual architecture

| Area | Source implementation |
| --- | --- |
| App | Kotlin activities, Material Components, runtime/Studio download/install, status, authentication experiments |
| JNI | Container probe/run/stop, archive extraction, launch argv/env/cwd and FEX JSON generation |
| Container | C++ user/mount namespace path or proot; explicit argv/environment |
| Host | Ubuntu 24.04 ARM64 glibc userland, utilities, X11 packages, graphics libraries and helper scripts |
| Translation | Separately built ARM64 FEX; explicit execution rather than root binfmt_misc registration |
| Guest | Ubuntu 24.04 x86_64; distro Wine plus Kombucha, DXVK 2.4 and prepared prefix |
| Display | Xvfb framebuffer file → Kotlin byte/int arrays → bitmap → Surface canvas |
| Input | Activity event translation → queue/TCP loopback → Python XTEST helper |
| Audio | Design/dependencies; no complete Android AudioBackend |
| Studio | Official version/CDN lookup, ZIP package mapping/extraction and executable discovery |
| CI | Native tests, APK job, manual runtime builds/releases, optional FEX thunks and Mesa builds |
| Probes | Kernel/ABI/page size/namespace checks plus shell and basic Vulkan probes |

The intended chain is Android → container → ARM64 glibc/FEX → x86_64 Wine → Studio.
DXVK/Vulkan thunks and GPU presentation are intended, but the inspected source does not
establish the complete accelerated Android graphics path. M2 documentation explicitly
labels FEX/Wine device execution unverified. See
[architecture](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/docs/ARCHITECTURE.md),
[M2](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/docs/M2-FEX-WINE.md), and
[actual runtime](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/app/src/main/java/dev/vodka/runtime/StudioRuntime.kt).

## Concepts to retain

Separate host/guest rootfs, explicit translator invocation, rootless directory rootfs,
Kotlin/native boundaries, structured process arguments, capability-based backend choice,
independent runtime recipes, small execution milestones, and official Studio retrieval.
FEX needs matched guest and host thunk installation; a host thunk directory alone is not
proof of forwarding. Separate ARM64 instruction compatibility from glibc/Bionic ABI work.

## Weaknesses and unfinished areas

| Evidence | StudioDroid consequence |
| --- | --- |
| app/build.gradle.kts targets API 28 | Does not prove modern-target execution; require M3 app-UID tests |
| Namespace probes stop before complete mount/exec setup | Test the actual operation; PID namespaces are probed but not used in the implemented launch |
| Proot presence is largely an executable-path check | Proot is not universally available; run a real ptrace/loader test |
| Proot unit test substitutes echo | Passing proves exec plumbing, not real proot compatibility |
| Stop targets one PID; scripts use name-based pkill | Own/reap the full process tree and helpers; use bounded shutdown |
| JNI maps signals to a generic negative result | Retain signal, exit code, errno and failure phase |
| Rootfs install deletes old target before rename/copy; FEX/Mesa merge into live trees | Use immutable versions and recoverable activation |
| Install/download reuse checks sizes; flow does not verify supplied SHA-256 sidecars | Authenticate metadata and verify bytes |
| Extractor accepts arbitrary symlink targets; O_NOFOLLOW is only on final file | Validate each parent component with directory FDs; prevent symlink escape |
| Duplicate names overwrite; premature header-read termination can return success | Reject collisions and malformed/truncated input |
| Long-name/PAX entries allocate archive-controlled lengths | Bound metadata and expanded resource use |
| Log watcher allocates full delta; final tail first reads whole file | Bounded queues/reads and disk rotation |
| CPU bitmap presentation and incomplete Surface restart logic | StudioDroid-owned NativeSurfaceBackend with tested lifecycle/low-copy transport |
| Unbounded input queue and activity-bound controls | Reusable bounded event pipeline |
| Exported activity accepts arbitrary execution/script automation extras | No release arbitrary-command entry point |
| Authentication experiments save security cookies to a file | No cookie harvesting/plaintext credential path |
| Moving FEX/Winetricks sources, unpinned apt/image inputs | Reproducibility requires immutable inputs, not only scripts/checksums |
| FEX workflow defaults thunks off; broad host payload and duplicate Wine packages | Minimal dependency closures and explicit artifact capability checks |

Sources: [container](https://github.com/Shellworks-Development/vodka/tree/ca41f2b/native/container),
[tests](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/native/tests/container_test.cpp),
[archive](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/native/archive/src/archive.cpp),
[installation](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/app/src/main/java/dev/vodka/runtime/RootfsManager.kt),
[session](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/app/src/main/java/dev/vodka/runtime/VodkaSession.kt),
[display](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/app/src/main/java/dev/vodka/DisplayActivity.kt),
[app flow](https://github.com/Shellworks-Development/vodka/blob/ca41f2b/app/src/main/java/dev/vodka/MainActivity.kt),
[workflows](https://github.com/Shellworks-Development/vodka/tree/ca41f2b/.github/workflows).

## Limits and decisions

README/docs differ from source about Debian versus Ubuntu, target SDK, Wine distribution,
prefix placement and presentation. Do not reuse milestone claims as test evidence.
Neither the CPU-copy display path nor the authentication/proxy experiments belong in the
production design. Vodka is GPLv3; any future adapted code needs its notices and source
obligations. M0 uses research only. The [presentation correction](../PRESENTATION_OPTIONS.md)
is binding over any reference design.
