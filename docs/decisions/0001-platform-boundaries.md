# ADR 0001 — Platform and presentation ownership

Status: Accepted for M0, including the user's presentation correction.

## Context

StudioDroid v2 is a clean Android ARM64 project targeting the genuine Windows x86_64
Studio executable. Reference launchers provide research, not a platform to inherit.

## Decision

Use Kotlin/Material 3 with app/core/runtime-android boundaries and a runtime-independent
controller. Implement FEX first; reserve experimental Box64. Keep host ARM64 glibc,
x86_64 guest, Wine, DXVK and Studio component ownership separate. Distinguish translation
from glibc/Bionic interoperability. Use GPL-3.0-only for StudioDroid-owned work.

PresentationBackend has StudioDroid-owned NativeSurfaceBackend. It owns Android surfaces,
lifecycle, resize/orientation/synchronization, native overlays/touch controls, mouse
capture, IME, fullscreen and low-copy presentation. Wine protocol compatibility is not
an Android backend. Optional StudioDroid-owned internal X11 compatibility or a validated
native/direct transport remains OPEN until M6. No Termux:X11 app/runtime or external
X server process dependency; no production Xvfb CPU bitmap path. Input mapping remains
reusable outside activities. See [presentation contract](../PRESENTATION_OPTIONS.md).

## Consequences and alternatives

No FEX calls in UI; no server-protocol selection exposed as Android presentation.
No custom Mali driver now. Native/glibc/GPU transport remains a real research gate, not
solved by a library filename. Separate interfaces support future providers without
pretending those providers exist. Avoid legacy internals and wholesale reference copying.
