# StudioDroid v2 architecture

Status: approved M0 design with the M1 launcher/control-plane foundation. Execution
backends and physical-device qualification remain unavailable.

## Product and boundaries

Run genuine, unmodified Windows x86_64 Roblox Studio on Android ARM64. Priorities, in
order: stability, compatibility, low memory use, performance, maintainability, usability,
and extensibility. Four-gigabyte physical-RAM phones are a required target. Do not assume
root, large swap, fast storage, Snapdragon GPUs, or unlimited background execution.

The first Gradle modules, introduced at M1, will be:

| Module | Responsibility |
| --- | --- |
| `:app` | Kotlin/Material 3 Views, navigation, ViewModels, Android document pickers |
| `:core` | Platform-independent models, policies, interfaces, structured launch planning |
| `:runtime:android` | Android capabilities, persistence, payload transactions, backend adapters, JNI and supervision |

The application depends on the runtime controller, never FEX-specific APIs. A composition
root selects implementations. Native responsibilities remain separately organized without
creating a separate Gradle module for every class.

```text
Android launcher → RuntimeController
                   ├─ capability/profile policy
                   ├─ PayloadManager
                   ├─ LaunchPlanner
                   └─ native session supervisor
                        → ContainerBackend
                        → ARM64 glibc environment
                        → RuntimeBackend (FEX initially)
                        → x86_64 guest / Wine
                        → genuine Roblox Studio
```

There are two distinct ABI boundaries: x86_64 ↔ ARM64 and glibc ↔ Android Bionic.
FEX library thunking does not automatically bridge the latter. A compatible Vulkan
loader, driver transport, window-system integration, and synchronization remain necessary.

## Runtime, container, and state

See [Runtime backends](RUNTIME_BACKENDS.md). Implement FEX initially; reserve Box64 as
future/experimental without a fake success implementation. Namespace and proot backends
must pass actual app-UID operation tests. Neither constitutes a security sandbox.

```text
Unavailable → NeedsSetup → Ready
Ready → Preparing → Starting → Running → Stopping → Exited
Active operation → Failed
Interrupted installation/session → RecoveryRequired
```

Installed, validated, started, and healthy are different facts. A launch is associated
with a unique session ID, exact component digests, immutable validated configuration, and
readiness evidence. The supervisor owns child processes, descriptors, required helper
services, and bounded shutdown. UI recreation must never create a second runtime session.

## Presentation ownership (binding correction)

```text
PresentationBackend
└── NativeSurfaceBackend
```

NativeSurfaceBackend is owned by StudioDroid. It owns Android Surface / SurfaceView /
SurfaceControl / ANativeWindow integration, lifecycle, resizing, orientation,
synchronization, native UI overlays, touch-control integration, mouse capture, IME
integration, fullscreen, and low-copy buffer presentation. Reusable input translation
and control-layout logic remain outside activities.

Wine/X11 protocol compatibility is not the Android presentation backend. The exact
transport remains OPEN until M6 validates one of these paths:

```text
Wine → optional StudioDroid-owned internal X11 compatibility layer
     → GPU/shared-buffer transport → NativeSurfaceBackend → Android Surface

Wine → native/direct transport → NativeSurfaceBackend → Android Surface
```

Any required X11 compatibility server/transport is implemented or embedded internally
behind NativeSurfaceBackend. It is not a user-visible backend, external server process,
or separately installed runtime requirement. Production must not depend on the Termux:X11
application or runtime. Termux:X11 remains research-only. Xvfb with CPU bitmap copying
is excluded from production. See [Presentation research gate](PRESENTATION_OPTIONS.md).

## Graphics and driver boundaries

| Contract | Responsibility |
| --- | --- |
| GraphicsBackend | D3D translation and supported options; DxvkBackend initially |
| VulkanDriverProvider | System, bundled, or custom driver identity, ABI and loading requirements |
| VulkanTransport | Linux graphics stack ↔ Android driver bridge, independently qualified |
| PresentationBackend | StudioDroid-owned Android presentation, NativeSurfaceBackend |
| RendererProfile | Default, Compatibility, LowMemory, Performance, Custom settings |
| DxvkManager | Version install/update/repair/rollback and compatibility notes |

Driver descriptors carry ABI/libc and transport requirements, not merely a `.so` path.
DXVK channels are Stable, Compatibility, Experimental, and Custom; Stable means qualified
for a StudioDroid configuration, not automatically the newest upstream release. Store
per-version Vulkan features/limits, Wine requirements, and thunk compatibility.

Use system Vulkan by default on Adreno, Mali, PowerVR, and Unknown/Other GPUs. Future
Turnip packages require compatible Adreno hardware; never select them for Mali.
Custom Mali driver development is a separate future project. This repository specifies
only integration contracts and [driver safety](DRIVER_PACKAGES.md).

Isolate DXVK, shader, Vulkan pipeline, and FEX caches by relevant component/driver/device
fingerprints. Report sizes and permit clearing/budget changes. Disk cache limits are not
GPU-memory limits; do not promise hard control over opaque driver caches. Never unlink
active caches; apply unsupported live changes on the next session.

## Capabilities and memory

Probe ABI, Android version, page size, kernel execution capabilities, physical/available
RAM, readable pressure/swap information, GPU vendor/model, SoC, Vulkan API/extensions,
features/limits and driver version. Unknown values remain unknown, with conservative
defaults. Record probe origin, errors, and time; invalidate cached results after OS,
driver, or runtime updates. Intrusive/driver probes use disposable supervised processes.

Profiles choose defaults only. [Low-memory policy](LOW_MEMORY.md) defines bounded I/O,
logs, caches, concurrency, sampling, and the 4 GB acceptance workload. Android Java heap
limits are not Wine/FEX memory limits. Measure the complete session without double-counting
shared GPU/system allocations.

## Components, Studio, and storage

PayloadManager manages ARM64Rootfs, X86_64Rootfs, FEX, Wine, DXVK, GraphicsDrivers, and
Studio independently. Separate immutable components from Wine prefixes, Studio versions,
projects, caches, and session state. Rootfs is installed once per version, not every launch.
Do not require FUSE, overlayfs, or filesystem reflinks. Use [recoverable activation](PAYLOAD_FORMAT.md).

StudioDownloader, StudioInstaller, StudioVersionManager, StudioLauncher, and
StudioRepairManager belong above the runtime. They support first install, update, repair,
reinstall, clean remove and version metadata. Optional version selection requires a
legitimate available source and qualified compatibility. Parse complete official package
records and reject unknown installation layouts rather than guessing. Do not redistribute
Studio or modify its executable. Wine/DXVK setup is separate from Studio feature logic.

Keep internals in app-managed storage. SAF document URIs are not POSIX filenames: import
user projects into a private working directory and export using Android document APIs.
Removal of Studio/runtime components must not silently remove user projects. Broad storage
permissions are excluded unless a later concrete requirement is approved.

## Input, audio, and network

Reusable input events cover pointer motion/buttons, scrolling, physical keys, text/IME
composition, gestures, shortcuts, and configurable touch layouts. Coalesce motion while
preserving button/key transitions; release captured input on detach/focus loss. The
presentation backend owns Android integration and overlays, not hardcoded activity logic.

AudioBackend owns initialization, capability reporting, start/stop and diagnostics with
bounded buffers. The first research candidate is Wine's PulseAudio-facing path bridged
to Oboe/AAudio. Start only required services and restart/stop them with the session.

Use normal Android networking and account for network changes, VPNs, DNS and TLS.
Do not inherit hardcoded DNS servers, always-on proxies, cookie harvesting, or plaintext
authentication files. Keep authentication in genuine Studio or a validated supported
handoff. No login integration is implemented through M4.

## Recovery, diagnostics, and user experience

Persist a session journal before spawning any process. Record phases, exit/signal/startup
errors, component versions, memory samples, and device details. Logs are structured,
bounded and rotated; all requested categories are defined in [Security](SECURITY.md).
Export a single bounded redacted diagnostic package, never project contents or credentials
by default. Raw native logs retain their source/category even when parsing fails.

Two consecutive startup failures for a configuration offer safe mode. Attribute driver
failures separately from user stop, cancellation, and unknown system termination. Do not
auto-relaunch. Candidate drivers can be quarantined without loading them during launcher
recovery. Safe mode selects system Vulkan, qualified stable DXVK, conservative runtime and
renderer settings, custom drivers disabled, and optional cache reset.

Last-known-good is an exact qualified component/configuration set, not a PID that survived
spawn. Through M4 it requires a completed probe and clean shutdown; later Studio requires
a responsive window, initialized graphics and sustained operation. Prefix migrations need
their own reversible backup/compatibility plan. Project files are never part of rollback.

Normal users see install state, readiness, profile and launch guidance. Advanced users
can inspect backend/driver/DXVK selections and supported settings. Planned navigation:
Home, Studio, Graphics, Drivers, Runtime, Controls, Storage, Logs, Diagnostics, Settings,
About. Through M4 only Home, Runtime, Storage, Logs, Diagnostics, Settings, About are
functional; Studio launch remains unavailable. Developer views expose redacted launch
plans, environments, FEX/Wine configuration and diagnostics, never arbitrary shell input.

## Repository shape and evidence

```text
app/                         Kotlin UI (M1+)
core/                        contracts and policies (M1+)
native/                      archive, capabilities, container, protocol, supervisor
runtime/android/             adapters, JNI, persistence (M1+)
runtime/{fex,proot}/          independent source build recipes (M3/M4+)
runtime/rootfs-{arm64,x86_64}/ independent environment recipes (M3/M4+)
runtime/manifests/           M0 research-only metadata; no installable artifacts
schemas/                     M0 declarative contracts
tools/{device-probe,runtime-smoke}/  physical-device evidence (M3/M4+)
scripts/                     independent packaging, verification, SBOM tools (M2+)
docs/{research,decisions}/    source evidence and binding decisions
.github/workflows/           independent CI jobs (M1+)
```

Later Wine, DXVK, driver, presentation, audio and input implementations are gated by
measured results. See [Roadmap](ROADMAP.md), [Acceptance](ACCEPTANCE.md), and
[Build and CI](BUILD_AND_CI.md). No empty/future module implies a working implementation.
