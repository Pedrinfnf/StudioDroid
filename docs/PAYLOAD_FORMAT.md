# Payload and metadata contracts

Status: M0 schemas and policy only. No catalog, downloadable payload, installer, or
qualified configuration is supplied. JSON schemas use Draft 2020-12 and schemaVersion 1.
Unknown schema versions fail closed. Future format changes require explicit migrations.

## Source inventory is not a release lock

[runtime/manifests/sources.lock.json](../runtime/manifests/sources.lock.json) deliberately
has state `research-only`, buildable false, and artifacts empty. It records immutable
inspected repository commits, roles and unresolved build inputs. It is not an executable
build lock despite the reserved filename. Build tooling must reject it until a reviewed
locked state includes source/submodule pins, toolchain/container digests, signed package
snapshot coordinates and artifact hashes. Never fill missing hashes with placeholder zeros.

[runtime/manifests/compatibility.json](../runtime/manifests/compatibility.json) has state
`unqualified`, qualifiedConfigurations empty, and unverified runtime/container entries.
System driver defaults and memory profile rules are policy, not GPU support claims.
All four GPU classes are represented. Box64 is future/experimental. Presentation ownership
is native-surface, internal transport is unresolved, qualification milestone is M6.

M0 invariants: schemaVersion 1; source IDs unique; repository pins are 40 lowercase hex
characters; source URLs are HTTPS; research-only buildable is false with zero artifacts;
unresolved build inputs are nonempty; compatibility contains no qualified combinations;
all runtime/container entries remain unverified or future-experimental. Production cannot
consume either document as a list of launchable/installable artifacts.

## Release manifest

[payload-manifest.schema.json](../schemas/payload-manifest.schema.json) describes immutable
release identity: id/name/kind/version, architecture, ABI/libc, source URL/revision,
artifact SHA-256 and byte counts, delivery/format, compatibility, exact dependencies,
entry points, licenses and an external file inventory descriptor.

Payload kinds are ARM64Rootfs, X86_64Rootfs, FEX, Wine, DXVK, GraphicsDrivers, Studio.
APK-native delivery explicitly represents host executable packaging; it is not a claim
that downloaded archives can exec from writable private storage. Through M4, host
entry points can remain APK-delivered while guest/data components update independently.
Studio records describe official runtime retrieval, never permission to redistribute.

Keep large per-file inventories in separately hashed, bounded, streamed metadata rather
than one giant in-memory manifest. Each inventory record declares normalized path, type,
size, digest and contained link target where permitted. Reject duplicate/colliding paths.
Declared sizes are checked against actual bytes with limits and overflow-safe arithmetic.
Metadata/signature parsing has a 1 MiB initial budget per document; inventories are streamed.
JSON duplicate object keys must be rejected before schema validation (ordinary parsers
may silently retain only the last value).

Use a standard detached catalog signature with an app-shipped trust root and audited
verification implementation. Lock algorithms/keys and rotation/freshness policy when
catalog delivery is implemented; M0 contains no invented production key or signature.
SHA-256 payload identity is mandatory. An asset-side checksum from the same untrusted
source is not independently authenticated metadata. Source/build pins require real values
before generating distributable payload manifests.

## Local component state

Local records are separate from immutable release manifests. They include expected and
observed checksums, last verification time, installed bytes, update/repair status, source
metadata and install transaction ID. State progresses from NotInstalled through Downloading,
Downloaded, Verifying, Extracting, Installed, Validated and optionally Active/Quarantined;
failures and interrupted operations remain explicit. A marker file or matching size alone
cannot establish installation readiness.

```text
private/
├── components/<kind>/<version>-<digest>/
├── staging/<transaction-id>/
├── downloads/<digest>.part
├── sessions/<session-id>/
├── prefixes/<prefix-id>/
├── studio/<version>/
├── projects/
├── caches/<component-fingerprint>/
└── metadata/
```

## Installation and update transaction

1. Validate authenticated metadata, dependencies, ABI and available storage.
2. Budget compressed download + expanded candidate + retained working version + journal
   overhead. On insufficient storage offer cleanup; never delete known-good automatically.
3. Stream/resume the download into a partial file. Validate HTTP status/range semantics,
   identity and complete length; never append unrelated content to a resumed download.
4. Verify the received digest before activation, then extract into a unique staging tree.
5. Enforce [archive policy](SECURITY.md) and validate full inventory/required entry points.
6. Synchronize data and rename on the same filesystem into immutable component storage.
7. Atomically update active configuration metadata using SQLite transactions; synchronize
   directories and reconcile filesystem/database states after interruption.
8. Retain the working version until the candidate passes health qualification. Garbage
   collect only unreferenced, inactive versions; rootfs is never re-extracted per launch.

Do not merge updates into live rootfs or copy the entire runtime to compose a session.
Mount/reference components without assuming overlayfs, reflinks or FUSE. Rootfs links must
be normalized and contained by the builder; temporary/proc/device exposure is a validated
runtime mount decision. Proot does not enforce kernel read-only bind security.

Wine prefixes are mutable state outside guest rootfs. A Wine update requiring migration
needs a separately recoverable prefix plan before activation. Projects are never rolled
back with runtime state. Reinstall/repair/remove affect selected components, not everything.

## Other schemas and semantic validation

- [driver-manifest](../schemas/driver-manifest.schema.json): future package ABI/GPU/loader/
  Vulkan requirements, file hashes and notices; see [Driver packages](DRIVER_PACKAGES.md).
- [launch-plan](../schemas/launch-plan.schema.json): typed executable/path/FD/service plan;
  see [Runtime backends](RUNTIME_BACKENDS.md). `none` presentation supports M4 probes;
  only NativeSurfaceBackend may be a real Android presentation backend. `unresolved`
  transport is allowed only in drafts, never a validated native-surface launch.
- [session-report](../schemas/session-report.schema.json): bounded diagnostic metadata,
  phases, exact component identities, nullable observations and distinct exit outcomes.

Schema acceptance is necessary but insufficient: validate signature trust, actual ELF,
path containment, duplicate JSON keys/inventory entries, source allowlists, dependency
cycles, hardware features and live resources at the correct boundary. Schema formats
must be checked with a format-aware validator. A report is evidence only when produced
by the real supervised operation; passing a JSON test does not establish runtime health.
