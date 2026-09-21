# Future custom driver packages

Status: M0 contract only. No importer, driver loader or custom Mali implementation exists.
The separate future Mali project must satisfy the same ABI/device qualification contract.

## Package and metadata

```text
driver-package.zip
├── manifest.json
├── lib/
├── metadata/
└── licenses/
```

[driver-manifest.schema.json](../schemas/driver-manifest.schema.json) records schemaVersion,
id, name, version, author, source, architecture, ABI/libc, GPU vendor/families/models,
kernel interfaces, minAndroidApi, pageSizes, Vulkan API/extensions/features/limits,
driverType, loaderAbiVersion, transports, libraries with sizes/hashes, license files,
knownIssues and compatibilityNotes. Library paths are relative to lib/; licenses remain
inside licenses/. ZIP regular files/directories only in the initial custom-package policy.

Arm64 machine code alone does not imply compatibility: Android Bionic drivers, glibc
ICDs, kernel APIs, WSI and FEX thunk coverage must match. SystemDriverProvider,
BundledDriverProvider and CustomDriverProvider return validated descriptors and requirements,
not arbitrary dlopen paths or shell/environment scripts.

## Compatibility

Adreno, Mali, PowerVR and Unknown/Other start with system Vulkan. Turnip is eligible only
for specifically compatible Adreno combinations, never Mali. A package cannot declare a
universal GPU driver by omitting requirements. Unknown models do not imply compatibility;
conservatively leave custom activation unavailable until explicit evidence qualifies it.

VulkanDriverProvider is independent of GraphicsBackend, DXVK version/channel, renderer
profile and NativeSurfaceBackend. GPU memory/transport requirements belong to the validated
configuration, not guessed from a vendor string. A renderer plugin uses a versioned
contract; a native driver manifest is not a plugin-code execution permission.

## Safety and activation (M15+)

Import → bounded manifest parse → trust/checksum/ELF validation → controlled staging
extraction → compatibility validation → disposable-process probe → candidate activation
→ health qualification → known good.

Reject bad architecture, unsupported ABI/page size/loader version, absent required
libraries, path traversal, absolute paths, symlinks, duplicate entries and hash/size
mismatch. Match inventory to declared files and enforce total expanded-size limits.
Schemas only validate shape; executable parsing, trust, path resolution and GPU checks
are mandatory runtime work. Checksums prove content identity, not trustworthiness.

Keep imported native code out of the launcher UI process. Same-UID helpers provide crash
containment, not a sandbox against malicious native code. Explain provenance and trust
when importing custom components. Never auto-install from unknown mirrors.

After two attributed startup failures, quarantine the candidate and select the previous
qualified provider or system Vulkan. Never load a quarantined driver during launcher
startup/recovery. Switch providers via a fresh runtime process; no live dlclose/hot-swap.
Retain a separately persisted known-good pointer and the working component until the
candidate qualifies. Never delete an active/known-good version as automatic storage cleanup.

A kernel GPU hang may exceed userspace recovery capabilities. The launcher must remain
recoverable after restart and avoid retry loops; do not promise every GPU fault can be
recovered within the same process/session. See [Security](SECURITY.md).
