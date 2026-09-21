# ADR 0004 — 4 GB budgets and durable recovery from the start

Status: Accepted initial policy; performance targets require measurement.

## Decision

4 GB physical RAM is first-class. LOW_MEMORY/BALANCED/PERFORMANCE choose safe defaults;
unknown capabilities choose conservative behavior. Memory profiles do not promise every
Studio workload fits. Use bounded I/O/logging, serial heavy work on low-memory devices,
lazy UI/services, disk inventories/caches and complete session cleanup. No largeHeap or
swap-as-physical-RAM budgeting. See [initial budgets](../LOW_MEMORY.md).

Persist session ID, phases, exact component configuration and memory/exit evidence before
launch. The supervisor owns processes and helpers. UI recreation observes the existing
session. Failures remain explicit; no dummy success or automatic crash-loop relaunch.

Two consecutive startup failures offer safe mode. Attribute driver failures before
quarantine; cancellation/user stop/unknown OS exit are distinct. Keep known-good state
separate and promote only after milestone-specific health evidence. Use system Vulkan,
qualified stable DXVK and conservative settings in safe mode, custom drivers disabled.
Optional cache reset never affects projects. Prefix changes require separate recovery.

## Consequences

Memory/security/recovery foundations start in M1/M2 rather than waiting until M14/M17.
Measure steady-state and peak complete-session memory; do not confuse Android Java heap,
FEX virtual mappings and shared GPU allocation. Later release qualification requires a
30-minute small-place edit/save/reopen session on a physical 4 GB phone. Keep bounded,
redacted diagnostics even when the runtime is unusable. A PID alone is not known good.
