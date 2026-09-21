# StudioDroid v2 roadmap

M0 records the approved design only. M1 requires separate approval. No executable runtime,
APK, or physical-device qualification is claimed by any M0 file.

| Milestone | Deliverable / exit gate |
| --- | --- |
| M0 | Research records, architecture, threat model, ADRs, schemas, pinning policy, acceptance matrix; document/schema validation |
| M1 | Buildable Kotlin/Material 3 launcher; real capabilities, bounded logs/settings and lifecycle tests |
| M2 | Verified streaming payload installation, atomic activation, interruption, repair and storage tests |
| M3 | ARM64 environment executes under the modern-target app UID; tested container selection and cleanup |
| M4 | Physical-device FEX static/dynamic x86_64 execution, arguments, environment, child exec, signals, files and repeated cleanup; includes 4 GB device |
| M5 | Qualified Wine boot and console execution under FEX |
| M6 | Research selects internal Wine transport; StudioDroid-owned NativeSurfaceBackend presents a real Wine window with lifecycle recovery |
| M7 | Qualified Vulkan transport, matched thunks and D3D11 sample |
| M8 | Graphics profiles, DXVK channels/version selection and cache controls |
| M9 | Official Studio discovery/install/update/repair/reinstall/removal |
| M10 | Genuine Studio reaches login/start page |
| M11 | Reference place renders correctly in 3D |
| M12 | Keyboard/mouse/touch, IME, gestures, shortcuts and configurable controls |
| M13 | Audio, Android-aware networking and project storage round trips |
| M14 | Full 4 GB workload qualification and memory regression gates |
| M15 | Driver import/probe/activation infrastructure; no custom Mali driver development |
| M16 | Qualified independent runtime updates and prefix-aware rollback |
| M17 | Complete safe mode and diagnostics/recovery user experience |
| M18 | Measured startup/frame-time/power/thermal/storage tuning |
| M19 | Public beta with published tested-device matrix and known issues |

Memory controls, security, recovery journals and versioning begin with M1/M2. Later
milestones complete qualification; they do not postpone those foundations.

Through M4, exact implementation scope is app/core/runtime-android foundation, payload
transactions, native supervisor/container, minimal ARM64 and x86_64 environments, FEX and
owned execution fixtures. Wine, presentation, DXVK, drivers, audio and Studio implementation
remain gated. Interfaces/schemas are not permission to implement those later systems early.

M6 keeps the transport OPEN until measured: optional StudioDroid-owned internal X11
compatibility or a validated native/direct path, both behind NativeSurfaceBackend. No
Termux:X11 application/runtime or external X server process is a production dependency.
Xvfb plus CPU bitmap copying cannot satisfy M6/M7 production acceptance.

For each milestone: make small changes, compile after meaningful executable changes,
run relevant tests, retain useful logs, report errors plainly, and obtain approval at the
agreed boundary. See [Acceptance](ACCEPTANCE.md) and [Build and CI](BUILD_AND_CI.md).
