# ADR 0003 — Versioned components and recoverable activation

Status: Accepted; implementation starts at M2.

## Decision

Independently identify ARM64Rootfs, X86_64Rootfs, FEX, Wine, DXVK, GraphicsDrivers and Studio
by version, provenance and verified content hash. Separate immutable installs from mutable
prefixes, projects and caches. Directory rootfs is installed once per version; do not
require FUSE, overlayfs, reflinks or a complete session copy.

Authenticate catalogs, verify streamed bytes, extract into unique staging with controlled
paths, validate inventory, then same-filesystem rename and transactional metadata update.
Persist enough journal state to reconcile interruption across filesystem/SQLite boundaries.
Retain active/known-good components until a candidate qualifies; low storage is not
permission to delete them. Prefix migrations require a separate reversible state plan.

M0 source inventory is explicitly research-only/non-buildable with no artifacts; M0
compatibility has no qualified combinations. Release locks need real immutable source,
submodule, toolchain, package-snapshot and artifact pins. No invented checksums or signing
keys. Future native drivers carry ABI/GPU/kernel/loader compatibility and are probed in
disposable processes. See [Payload format](../PAYLOAD_FORMAT.md).

## Alternatives rejected

Live extraction/merge, delete-old-before-rename, matching sizes as integrity checks,
reinstall-everything updates, rootfs-embedded mutable prefixes, and one permanent DXVK
version prevent safe independent evolution. Reproducible scripts without locked inputs
are not reproducible artifacts.
