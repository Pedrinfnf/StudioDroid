# StudioDroid-owned presentation and M6 research gate

Status: ownership is ACCEPTED; internal transport is OPEN until M6 evidence selects it.
This correction takes precedence over any reference project's X11-first design.

```text
PresentationBackend
└── NativeSurfaceBackend
```

## Ownership

NativeSurfaceBackend owns Android Surface / SurfaceView / SurfaceControl / ANativeWindow
integration, lifecycle, resizing, orientation, synchronization, native UI overlays, touch
controls, mouse capture, IME integration, fullscreen and low-copy buffer presentation.
Reusable input mapping/composition/layout logic is outside activities and feeds this
integration. Android presentation is not synonymous with Wine's display protocol.

```text
Wine → optional StudioDroid-owned internal X11 compatibility layer
     → GPU/shared-buffer transport → NativeSurfaceBackend → Android Surface

Wine → native/direct transport → NativeSurfaceBackend → Android Surface
```

If Wine requires X11, implement or embed StudioDroid's own internal compatibility
server/transport behind NativeSurfaceBackend. It is not the public presentation backend,
an external X server process dependency, or a separately installed application/runtime.
No Termux:X11 application or runtime dependency is permitted. Termux:X11 is research-only.
Xvfb plus CPU bitmap copying is excluded from production.

## Research candidates, not selections

| Internal transport candidate | Evidence required |
| --- | --- |
| Owned/embedded X11 compatibility with shared GPU buffers | Wine protocol/window behavior, required X extensions, matching WSI/buffer allocation and sync |
| Native/direct path using validated Wine interfaces | Existing driver/transport suitability, Win32 window/dialog behavior and Android buffer ownership |
| Wayland-based internal compatibility if warranted | Owned compositor/transport feasibility and measured advantage without external runtime dependency |

No candidate changes NativeSurfaceBackend ownership. Do not assume Android APIs, shared
memory, dmabuf availability, or a Vulkan version alone establish a zero-copy path.
Termux:X11's research code demonstrates both AHardwareBuffer import and CPU fallbacks;
its app/runtime is not a component to embed or require. See
[buffer research](https://github.com/termux/termux-x11/blob/bd1cfadd74f98b548662a48b6e1d564aa434c86e/lorie/src/main/cpp/lorie/buffer.c).
FEX thunking and glibc/Bionic Vulkan transport remain separate dependencies.

## M6 decision procedure

Use minimal Wine windows/GDI, menus, dialogs and accelerated samples on physical ARM64
hardware. Measure complete buffer paths, CPU copies, GPU copies, memory, latency, frame
pacing and synchronization. Prefer the least complex low-copy approach that actually
passes compatibility/lifecycle requirements; do not select by benchmark alone.

Test Surface detach/reattach, rotation, resize, density changes, fullscreen, IME/overlay
layout, mouse capture, Android recreation/background/resume and driver/presentation failure.
Surface loss stops submissions and releases/retires owned resources safely; it must not
spawn another runtime. Reattach the existing session when possible. Never submit through
a stale ANativeWindow or hot-swap a live driver.

Record the selected transport, alternatives, required libraries/licenses, buffer ownership,
fence semantics and device evidence in an M6 decision update. If no candidate passes,
report the gate unmet. Do not promote a CPU-copy Xvfb diagnostic into production success.
M0–M4 introduce no presentation implementation or transport dependency.
