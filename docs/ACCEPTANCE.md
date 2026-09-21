# Acceptance criteria and evidence

Status: M0 validation specification. No Android/FEX/Wine/Studio/device success is recorded.
All later milestones require real results; Unknown and Skipped are distinct from Passed.

## M0 completion

Required files: root LICENSE/NOTICE; updated README, .gitignore and existing architecture/
reference/roadmap/runtime docs; two research reports; LOW_MEMORY, SECURITY, PAYLOAD_FORMAT,
PRESENTATION_OPTIONS, DRIVER_PACKAGES, BUILD_AND_CI, ACCEPTANCE; four approved ADRs; four
Draft 2020-12 schemas; research-only source inventory and unqualified compatibility policy.

Validate JSON parsing (including duplicate keys), schema meta-validation, representative
valid/invalid instances, local Markdown links, whitespace, source-pin format and metadata
invariants, license integrity, and M0-only file scope. A temporary standard validator or
scratch test harness is tooling, not an application/runtime component. Do not add app code,
Gradle/CMake files, workflows, installers or runtime build scripts in M0.

Record exact created/modified files, git diff --stat and git status. Normal git diff omits
untracked files; report their statistics separately without staging merely for display.
After M0 stop and await explicit M1 approval.

## M1–M4 test matrix

| Area | Required scenarios |
| --- | --- |
| Application | Real capability/unsupported states, UI recreation, no duplicate sessions, bounded logs |
| Metadata | Invalid signatures, hash/size/ABI mismatch, unknown schema, duplicate object keys, untrusted sources |
| Download | Disconnect/resume, incorrect ranges, changed content identity, cancellation and insufficient space |
| Extraction | Traversal/absolute paths, parent symlinks, hardlinks, duplicate/case collisions where relevant, corruption/truncation, oversized metadata/dictionaries, entry/expanded-byte limits |
| Transactions | Process death before/after extraction, rename and metadata commit; reconcile without deleting working version |
| Plans | Argument boundaries, Unicode paths, denied environment keys, escaped/invalid mounts and stale FD roles |
| Container | Actual namespace setup or tested proot fallback, both unavailable, expected errno and cleanup |
| Supervisor | Stop/cancel/child death, helper reaping, interruption, forced app death and startup reconciliation |
| Logging | Flooding, bounded queues/tails, rotated files, full disk, redaction and dropped-message accounting |
| FEX | Static/dynamic guest execution, deterministic result/nonce, argv/env, child exec, signals and scratch file I/O |
| Memory | Physical 4 GB device, LOW_MEMORY selection, measured launcher/installer budgets and cleanup |
| Reproducibility | Locked inputs, independent repeat builds, ELF ABI/loader and APK page-size packaging checks |

The execution proof is a StudioDroid-owned C fixture built as static and dynamic x86_64
ELF. Verify machine type and digest before launch. It echoes a challenge nonce, checks
args/env, performs bounded allocation/arithmetic, writes only session scratch, spawns and
execs a child, and supports normal/nonzero/signal/long-running-stop paths. Twenty start/
stop cycles must not accumulate children, descriptors or required helper services.

Mandatory M4 proof is from the installed modern-target APK's own UID/context with SELinux
enforcing, no root/Shizuku or obsolete target workaround. Include a physical 4 GB ARM64
phone. ADB/Termux-shell, desktop Linux ARM64, QEMU or stub execution may aid investigation
but cannot satisfy this gate. A packaged initial FEX process must also demonstrate helper
and re-exec behavior. Record SDK/target/page size, backend, payload digests and resource data.

## Later release qualification

M6 keeps internal transport OPEN until measured Wine compatibility, memory, latency,
buffer-copy and lifecycle results choose it. NativeSurfaceBackend remains the sole owned
Android presentation implementation. Internal X11, if needed, is hidden behind it; no
external X server process or Termux:X11 app/runtime dependency is allowed. Xvfb CPU bitmap
copying cannot satisfy production acceptance. Test Surface recreation, rotation/resize,
IME/overlays, mouse capture, fullscreen and resumed sessions without duplicate runtimes.

M7 proves D3D11, Vulkan feature/limit requirements, matching FEX thunks and the glibc/Bionic
transport, not just VkInstance creation. M10/M11 require genuine Studio login/start and
correct reference-place 3D rendering. Do not substitute a mock Studio UI or modified PE.

M14's 4 GB reference workload: login, open a small versioned reference place, edit a part,
save/reopen and operate for 30 minutes. Record RAM/PSS/GPU accounting, startup time, frame
time distribution, CPU use, thermal behavior, disk/extraction/cache costs and shutdown.
Keep OS/background conditions and workload/version identifiable. All-device/all-project
support cannot be inferred from one successful test. Qualify low/mid/high RAM, Adreno,
Mali and PowerVR/Other, and 4 KiB/16 KiB execution separately as hardware becomes available.

## Risks and stop gates

| Risk | Evidence needed before proceeding |
| --- | --- |
| Modern Android execution/SELinux/seccomp | Real M3 executable/loader/container operation under app UID |
| FEX helpers or page-size mismatch | Real M4 dynamic/re-exec/signal tests; no alignment-only claim |
| glibc ↔ Bionic Vulkan | Qualified transport, buffer ownership, WSI and feature behavior |
| Mobile GPU/DXVK gaps | Version-specific required features/limits and rendering results |
| Studio/Wine/login changes | Genuine versioned qualification and supported authentication flow |
| 4 GB working set and thermal limits | Sustained complete-session measurements, graceful insufficient-memory handling |
| Storage/update interruption | Preserved working installation and prefix/project recovery |
| Driver failures/kernel GPU hangs | Disposable probes, quarantine, clean launcher recovery; limits disclosed |

M3/M4 failure requiring a new major execution architecture stops the tranche for a revised
review. Lack of a device is an unmet qualification gate, not a fake pass. Safe mode cannot
invent a compatible driver or make an oversized Studio workload fit in memory.
