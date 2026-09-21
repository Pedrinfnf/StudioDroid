# Low-memory architecture

Status: approved initial defaults and measurement targets, not observed memory results.
4 GB physical RAM is a required qualification class. No design promises that every
Studio project or background-app combination fits in 4 GB.

## Automatic profile

- LOW_MEMORY when reported physical RAM is below 5 GiB or Android reports a low-RAM device.
- BALANCED for other devices by default.
- PERFORMANCE only with at least 9 GiB reported RAM and a qualified device/graphics combination.
- Unknown RAM/capabilities select conservative LOW_MEMORY defaults. No M0 device is qualified.
- User overrides affect preferences; integrity, architecture, missing feature checks and
  measured inability to launch remain non-bypassable validation constraints.

GiB/MiB/KiB mean powers of 1024. Marketing RAM and OS-reported physical RAM can differ.
Never add swap/zram capacity to physical RAM when selecting the profile.

| Resource | LOW_MEMORY | BALANCED | PERFORMANCE |
| --- | --- | --- | --- |
| Concurrent downloads | 1 | 2 | 2 |
| Concurrent extractions | 1 | 1 | 1 |
| Large I/O copy buffer | 64 KiB | 128 KiB | 128 KiB |
| Aggregate queued log data | 256 KiB | 512 KiB | 1 MiB |
| UI log tail | 128 KiB | 256 KiB | 512 KiB |
| Aggregate graphics disk-cache target | 128 MiB | 256 MiB | 512 MiB |
| FEX disk-cache target, where supported | 64 MiB | 128 MiB | 256 MiB |
| Retained diagnostic logs | 16 MiB | 32 MiB | 64 MiB |
| Initial future render size / cap | 720p / 30 FPS | 900p / 30 FPS | 1080p / 60 FPS |

These are defaults, not promises that every upstream component supports a hard cache/FPS
limit. Unsupported options must be visible. Bound launcher-owned caches; perform safe
post-session eviction for opaque caches, and never delete an active pipeline cache.
Select actual resolution/aspect ratio only when a presentation backend is available.

## Launcher and installer budgets

On the 4 GB reference device, target steady-state aggregate launcher/control-plane PSS
below 128 MiB and installer incremental memory below 64 MiB. Record native, Java, shared
and graphics accounting methodology; avoid double-counting shared memory or adding a
separate fictitious VRAM pool to physical RAM.

Do not set largeHeap. Use lazy initialization, bounded queues, disk-backed inventories,
streamed HTTP/extraction, decompressor memory limits and paged log reads. Do not preload
Studio, start a desktop session, keep an embedded authentication WebView, retain archives
in memory, or duplicate rootfs trees per launch. Memory mapping is useful only with
bounded access/working-set accounting; mapping a file does not make it free.

On LOW_MEMORY, download and extraction heavy work are serialized; finish/pause installation
before launching the runtime. Release optional UI caches/services after launch. Retain
only required presentation/supervision/audio helpers, and stop them with their session.
Do not impose arbitrary RLIMIT_AS limits on FEX virtual-address reservations.

## Pressure observations and response

Sample available RAM before launch and every five seconds during active work. Increase
to one second temporarily while pressure persists; sample costly PSS less frequently.
Use Android MemoryInfo (including threshold/lowMemory), supported lifecycle/trim signals,
and readable per-process statistics. Swap/zram/PSI readings are optional observations;
permission denial means unknown, not zero. No privileged tuning or aggressive polling.

Recent Android versions no longer deliver several historic trim levels or onLowMemory.
Do not rely on those notifications as the only protection. Release UI caches on hidden
lifecycle transitions and use the supported APIs for each Android version.
[Android callback contract](https://developer.android.com/reference/android/content/ComponentCallbacks2).

Respond by dropping optional state, pausing recoverable I/O, and reducing only options
that support safe live adjustment. Estimate launch headroom from observed working sets
plus system needs; until measurements exist, report the estimate as unknown. Do not
invent a guaranteed Studio budget from physical RAM. Warn/refuse launch when evidence
shows insufficient headroom; preserve diagnostics on allocation failure. Do not forcibly
kill an editing session merely to meet a soft launcher budget or auto-relaunch after OOM.

## Qualification

M4 measures FEX proof execution on a physical 4 GB phone. M14 requires login, a small
versioned reference place, editing, save/reopen, and a 30-minute session. Measure peaks,
steady-state memory, frame times, heat and cleanup under repeatable background conditions.
Record failing workload/configuration explicitly; never label a device universally
supported from a hello-world test. See [Acceptance](ACCEPTANCE.md).
