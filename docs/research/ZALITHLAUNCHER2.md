# ZalithLauncher2 focused review

Inspected [1bef4ab9c49ee98db46c4fa39a6ea7b74dfac0b3](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab9c49ee98db46c4fa39a6ea7b74dfac0b3).
Scope: renderer/plugin interfaces, driver management/native helper, Vulkan capability
requirements, memory defaults, component/runtime installation, settings, launch integration,
and crash/log handling. No Minecraft-specific system or source is incorporated.

## Useful lessons

| Source area | Lesson for StudioDroid |
| --- | --- |
| RendererInterface / Renderers | Stable unique IDs, registry of implementations, lazy environment/library preparation |
| renderer_v2 configuration | Declarative selectable/toggleable/editable settings; avoid activity-specific controls |
| RendererEnv | Namespace settings and retire options removed by plugin updates |
| DriverPluginManager | Driver discovery is separate from renderer selection; installed package metadata can describe providers |
| VulkanCapabilities / VulkanDependency | Separate measured features/extensions from version-specific required/optional constraints |
| InstallableItem / runtime manager | Observable installation state and independently identified components |
| MemoryUtils / SettingsInitializer | Conservative defaults based on physical RAM, leaving room for the OS |
| Application crash path | Preserve useful crash evidence and a recoverable launcher entry point |

Sources: [renderers](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/game/renderer),
[renderer v2](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/game/plugin/renderer_v2),
[drivers](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/game/plugin/driver),
[capabilities](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/utils/device).

## Concepts that need stronger guarantees

- Renderer fallback to the first registry entry does not prove hardware compatibility.
  StudioDroid needs an explicitly qualified system/known-good fallback.
- APK metadata discovery does not establish package trust, kernel/ABI compatibility or
  a working Vulkan path. Validate and probe candidates outside the UI process.
- The inspected component/runtime installers delete destinations before replacement.
  Their behavior is not a transactional rollback model.
- Java heap-allocation formulas cannot budget Wine/FEX native or GPU allocations.
- Logger uses Channel.UNLIMITED and an expandable ByteArrayOutputStream fallback.
  A starting capacity is not a bound. StudioDroid must bound queues and fallback storage.
- Application image caches have explicit budgets, but do not establish a complete
  low-memory pressure controller for a Wine/FEX session.
- The reviewed areas do not establish automatic driver crash-loop quarantine and
  prefix-aware last-known-good rollback. Those are StudioDroid requirements, not claims
  about Zalith's implementation.

Sources: [runtime installation](https://github.com/ZalithLauncher/ZalithLauncher2/blob/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/game/multirt/RuntimesManager.kt),
[component installation](https://github.com/ZalithLauncher/ZalithLauncher2/blob/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/components/UnpackSingleTask.kt),
[memory defaults](https://github.com/ZalithLauncher/ZalithLauncher2/blob/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/setting/SettingsInitializer.kt),
[logger](https://github.com/ZalithLauncher/ZalithLauncher2/blob/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/utils/logging/Logger.kt),
[application](https://github.com/ZalithLauncher/ZalithLauncher2/blob/1bef4ab/ZalithLauncher/src/main/java/com/movtery/zalithlauncher/ZLApplication.kt).

## Attribution

The project uses GPLv3-family licensing and includes modification/branding notices in
its README. Some components carry inherited Pojav/FCL/HMCL attribution. Preserve exact
file notices and review applicable conditions if code is ever adapted. M0 draws on
architectural ideas only. See [NOTICE](../../NOTICE) and [references](../REFERENCES.md).
