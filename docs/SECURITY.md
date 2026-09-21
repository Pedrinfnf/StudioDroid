# Security, crash and recovery model

Status: M0 design and threat model. Runtime enforcement begins in later milestones.

## Assets and trust boundaries

Protect projects, credentials, runtime integrity, launcher availability and diagnostic
privacy. Inputs include network catalogs/downloads, imported archives, device reports,
configuration, Android intents, guest processes and future native driver packages.
Treat all external input as untrusted until validated. Android's app sandbox is the
baseline boundary; namespaces/chroot/proot are execution compatibility mechanisms.
Same-UID process separation contains crashes, not malicious native code with app access.

## Execution

No root, Shizuku requirement, global mounts, SELinux changes, arbitrary shell strings or
normal-user arbitrary execution. Use typed argv/environment/cwd/mount plans and a fixed
set of debug instrumentation operation IDs. No exported runtime command activity/service.
Only the normal launcher entry point is externally launchable. Validate all caller input.

Use app-private sockets, peer credentials and bounded versioned messages. Do not inherit
untrusted PATH, LD_PRELOAD, library paths or file descriptors. Resolve allowed executable
references from verified components and APK-installed native storage. Check actual ELF
architecture and compatibility, not filename extensions. Package/bootstrap execution is
subject to [the M3 gate](decisions/0002-modern-android-execution.md).

## Downloads, signatures and extraction

Authenticate runtime catalogs against an app-shipped trust root. Use a standard detached
signature implementation, separate offline/release signing keys, key rotation, freshness
and rollback protection. Runtime payloads use SHA-256, declared byte counts and complete
inventories. HTTPS and checksums alone do not authenticate a replacement catalog.
Release delivery of native executables is explicit, not a bypass hidden in a ZIP import.

Restrict formats in a maintained parser behind StudioDroid-controlled extraction policy.
Traverse using directory FDs and no-follow validation on every component. Reject absolute
names, traversal, normalized duplicates, corrupt/truncated archives, unsafe file types,
malformed metadata, integer overflow, excessive entry counts/expanded sizes and excessive
decompressor memory. Rootfs build tooling normalizes declared contained links; extraction
must reject unsafe symlinks/hardlinks and only allow validated in-installation targets.
Custom driver ZIPs initially permit no links at all. Never extract into live installations.

Hash the received archive before activation; inventory verification follows extraction.
Schema regexes cannot prevent symlink races or prove containment. Test runtime enforcement
with adversarial archives and fuzzing. Path errors must fail rather than silently skip
critical entries. See [Payload format](PAYLOAD_FORMAT.md).

## Session ownership and shutdown

Persist session ID, configuration fingerprint and phase before spawn. The supervisor
owns process groups/children, descriptors and all required helper services, including
FEX helpers. Reap children and prevent duplicated sessions. Request graceful shutdown,
then bounded termination of only owned processes. Do not use name-based pkill.

Record exit code, signal, startup errno and phase independently. A missing process may
mean Android/system termination; do not invent a signal or blame a driver. Use Android
exit information where available and retain Unknown when evidence is unavailable.

## Logs, diagnostics and recovery

Categories: ANDROID, CONTAINER, FEX, WINE, DXVK, VULKAN, DRIVER, STUDIO, INPUT, AUDIO,
NETWORK, STORAGE. Attach a unique session ID, timestamp and source to every launch log.
Queues, native stream readers, line lengths, UI tails and retained files are bounded.
Rotate on disk; when storage fails use a bounded fallback, preserve critical evidence and
record dropped-message counts. Never accumulate the full log in memory to obtain a tail.

Session reports include Android/device/SoC/GPU/Vulkan information, graphics provider,
DXVK/FEX/Wine/Studio versions, runtime profile, memory observations, last phase and outcome.
Missing values use null/unknown, never guessed success. Export one bounded diagnostic
archive without cookies, tickets, secrets or project contents by default. Redact sensitive
arguments, environment and user paths before export; keep runtime-internal plans private.

Two consecutive startup failures for the same configuration offer safe mode. Only
attributed driver initialization failures quarantine a driver. User stop/cancel and
unclassified OS termination do not automatically blame graphics. Do not auto-relaunch.
Store last-known-good separately; require completed execution health checks rather than
PID survival. Safe mode uses system Vulkan, qualified stable DXVK, conservative runtime/
renderer options and custom drivers disabled. Cache reset is optional, never a project reset.

Activate version updates only at safe session boundaries. Recover interrupted component
transactions at startup. Prefix migrations require separately recoverable state; a pointer
rollback cannot undo mutated prefixes. Never overwrite projects during rollback/repair.

## Platform and distribution limits

Respect scoped storage, explicit SAF user grants, modern foreground/background rules and
notification requirements. No unrestricted background-runtime promise. Long transfers
must be resumable; preserve progress if Android stops work. Do not harvest Roblox cookies
or save authentication secrets in plaintext files. No Roblox binaries in repo/APK/CI.

Future custom drivers execute native code and can affect the entire app UID; use provenance,
explicit import trust and disposable probes. Quarantine prevents repeated startup loading,
but cannot guarantee recovery from every kernel GPU fault. Keep launcher recovery free
of optional driver loads. Preserve licenses/source obligations in [NOTICE](../NOTICE).
