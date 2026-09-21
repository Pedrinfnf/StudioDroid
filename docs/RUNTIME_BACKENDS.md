# Runtime and container contracts

Status: M0 interface design; no Kotlin/C++ implementation is introduced here.

## RuntimeBackend

The app uses RuntimeController. A composition root provides RuntimeBackend adapters.
Implement FexBackend first; reserve Box64Backend for future experiments, never a stub
that claims execution. Backends must report Unsupported/Unavailable explicitly.

| Operation | Contract |
| --- | --- |
| initialize(context) | Resolve component references and initialize lazily; do not spawn a session |
| validate(request) | Return requirements, failures, warnings and evidence, not one guessed boolean |
| prepareEnvironment(request) | Produce bounded configuration files and validated environment |
| prepareLaunch(request) | Produce structured backend plan without starting execution |
| launch(validatedPlan) | Delegate to common supervisor and return a session handle or typed failure |
| stop(session, reason) | Idempotent graceful request followed by bounded escalation |
| getStatus(session) | Observe persisted/supervised state, not only kill(pid, 0) |
| getCapabilities() | Report guest ABI, host requirements, supported options, helpers and constraints |
| getVersion() | Return exact component version and digest |
| getDiagnostics(session?) | Return bounded structured evidence with explicit unavailable values |
| cleanup(session?) | Release owned helpers, descriptors and scratch resources without removing projects |

FexBackend owns version-specific FEX configuration serialization and validated options.
It must audit matched guest/host thunk libraries when graphics is introduced. Keep safe
upstream memory-model defaults; do not disable TSO to manufacture performance results.
The API must not expose FEX flags through activities or accept arbitrary command strings.

## Structured launch plan

[launch-plan.schema.json](../schemas/launch-plan.schema.json) describes a persisted plan,
not an executable shell script. A planner binds session ID, configuration fingerprint,
exact component hashes, runtime/container selections, typed host and guest executable
references, argv, environment, cwd, mount requests, service dependencies, FD slots,
readiness checks, resource policy, deadlines and diagnostics.

Host POSIX paths, guest POSIX paths, Windows paths, and Android document URIs are distinct.
Persist symbolic file-descriptor roles, never stale numeric descriptors. Re-resolve and
validate paths, digest/ABI, FD ownership, mounts, environment allowlists, and dependency
cycles immediately before execution. A schema-valid JSON file is not authorization to run.
The runtime validates the original plan; diagnostic exports redact secrets and are not
re-imported as executable plans.

M0.1 path shape rules apply to the main/helper cwd, guest target, mount target and
diagnostic directory. POSIX paths are absolute and normalized: `/` is valid, but dot
segments, empty components, trailing separators (except root), backslashes and C0/DEL
controls are rejected. Component-relative executable/mount-source paths use the same
component restrictions and cannot start with `/`.

Windows guest targets are drive-absolute or UNC paths. Drive paths accept Windows or
forward-slash separators; UNC paths begin with two backslashes and name a server/share.
Reject C0/DEL controls (including NUL, CR and LF), dot/empty components, trailing dots or
spaces, device namespaces, reserved delimiters and alternate data streams. Relative and
drive-relative Windows paths are not persisted. Runtime still validates Wine drive/share
mappings, reserved device names, symlinks, mount permissions and containment immediately
before use. Validating `/`, a mapped drive or a UNC share does not authorize access to it;
never treat string-prefix matching or schema acceptance as containment enforcement.

Cancellation cannot leave a half-active configuration. Reject concurrent launches for
one session/controller. UI reattachment observes an existing session; it cannot create a
second container. Preserve start errors, errno, signal and normal exit separately.

## ContainerBackend

NamespaceContainer and ProotContainer expose probe, validate, prepare, start, stop,
cleanup. Auto-selection prefers the lower-overhead namespace path only after the complete
operation passes in a disposable process under the installed app's UID:

1. Validate namespace identity mapping, mounts, required proc/dev exposure and executable
   loading, including any required PID namespace/reaping behavior.
2. Otherwise run a real packaged proot/ptrace/loader test; presence on disk is insufficient.
3. If both fail, show Unsupported with failing stage and errno. Do not fall back to root,
   Shizuku, permissive SELinux, unrestricted shell, or an obsolete target SDK.

Explicit backend preference cannot bypass capability validation. Mounts expose only
required resources. Proot's path virtualization does not enforce kernel read-only mount
security; record that limitation and treat component immutability as a controller policy.

## Modern Android execution gate

M3 tests packaged native supervisor/proot/bootstrap loader and host entry points from
APK-installed native storage, resolved through Android APIs. Rootfs data stays private.
Invoke an explicitly selected glibc loader where needed with structured argv. Verify
mapping, glibc loading, child execution and shutdown under modern target restrictions.

M4 also tests FEX helper discovery, FEXServer ownership, guest exec/re-exec, and process
cleanup. A successful initial FEX process does not prove helper execution. If the design
requires unacceptable platform bypasses or substantial new machinery, stop the milestone
with evidence and revise the design. See [execution ADR](decisions/0002-modern-android-execution.md).

## M4 execution proof

StudioDroid-owned static and dynamic x86_64 ELF fixtures echo a nonce, verify argument and
environment boundaries, perform deterministic arithmetic and bounded allocation, read/write
session scratch, spawn/exec another guest, and exercise exit/signal/long-running stop paths.
Verify ELF machine type and digest. No host-produced success text substitutes for guest
execution. See [Acceptance](ACCEPTANCE.md) for physical-device requirements.
