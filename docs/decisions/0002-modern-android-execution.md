# ADR 0002 — Modern Android execution must be demonstrated

Status: Accepted gate; packaged execution mechanism remains unverified.

## Decision

Signed sideloaded APKs first, with modern Android target SDK (initial target/compile 36,
minimum 29). Play Store qualification may be evaluated later. Never lower target SDK or
require root, Shizuku, permissive SELinux or external Termux execution to pass a milestone.

M3 tests packaged supervisor/proot/bootstrap loader and host entry points from APK-installed
native storage. Rootfs data stays private. Resolve paths through Android APIs; use explicit
glibc loader/argv when needed. Verify executable mappings, loader, mounts, child execution
and complete cleanup under the installed app's UID and platform restrictions.

Prefer NamespaceContainer only after complete capability probes pass; otherwise use a
real-tested ProotContainer. If neither works, report unsupported with stage/errno. Neither
proot presence nor namespace unshare alone proves the required operation. Neither is an
application security sandbox.

M4 proves FEX static/dynamic x86_64 execution, helper discovery, re-exec and signals on
physical hardware, including a 4 GB device. FEXServer and other required helpers must be
owned and reaped. Test 4 KiB/16 KiB compatibility separately; packaging alignment is not
execution proof. See [Acceptance](../ACCEPTANCE.md).

## Consequences

Native executable updates may initially require APK delivery while guest/data components
update independently. A source/build interface must expose delivery constraints. If the
mechanism cannot work without unacceptable bypasses or substantial new machinery, stop
at the gate and revise the design. Desktop/Termux/ADB-shell success cannot replace APK
context evidence. [Android restriction](https://developer.android.com/about/versions/10/behavior-changes-10).
