# M1 visual review

Scope: launcher presentation only, relative to `f229a3a`. No M2 implementation.
The user subsequently authorized QA commits/pushes on `m1/ui-preview` and GitHub builds.
The runtime/controller, capability probe, ViewModel, repositories, core contracts,
M0 schemas and validation harness remain unchanged.

## Visual system

A dark blue workspace with cyan identity, electric blue actions and limited purple
planning accents replaces the default Material surfaces. Original vector outline icons
and a small geometric StudioDroid mark require no bitmap assets or external font downloads.

Shared components: StatusChip, StatusRow, MetricCard, SectionHeader, RuntimeStage,
PrimaryAction, SecondaryAction, EmptyState, ProgressIndicator, TechnicalValue and
ProfileOption. StudioTheme owns colors, typography helpers, shape/ripple and motion rules.
PageBuilder composes screens; LauncherList virtualizes all screen rows using RecyclerView
and diffs immutable display descriptions. Logs use the same virtualized list, without a
nested scrolling list or a whole-file read. The original 16 KiB tail limit is retained.
Parsed display entries are reused until the tail changes and released when hidden.

Unknown is a neutral evidence state, not Error. Host observations and advertised Vulkan
never imply runtime readiness. Package installation states come from the existing records.
FEX/Wine/DXVK labels describe the planned path, not installed or executable functionality.
Presentation remains Wine -> unresolved internal transport -> NativeSurfaceBackend ->
Android Surface; the M6 transport decision and StudioDroid ownership are unchanged.

Memory bars distinguish physical/system usage, availability, process PSS and swap.
Storage bars describe the filesystem, not claimed runtime allocation. No fabricated log
messages, sessions, timestamps, installations or compatibility results are introduced.

## Accessibility, responsiveness and cost

- Wrapping text and content-sized rows; metric columns collapse for compact widths or
  larger fonts. Wide content is capped at 840 dp; drawer width adapts to the window.
- Explicit text badges, selected labels and checkable accessibility semantics. Interactive
  controls have at least 48 dp touch targets. Decorative icons are excluded from speech.
- Calculated contrast: primary text 15.60:1, secondary text 8.02:1, cyan status 12.07:1,
  purple status 8.68:1, primary action 5.03:1 and disabled action 6.76:1.
- Navigation fades last 150 ms. Android animation disablement and touch exploration disable
  these transitions. No blur, bitmap backgrounds, perpetual animations or new services.
- Only visible rows inflate views. Memory observations do not rebuild unchanged rows.
  No largeHeap, runtime preloading or new runtime cache is added.

## Reproduce

Use the unchanged M1 JDK/SDK/Gradle baseline in [BUILD_AND_CI](BUILD_AND_CI.md).

```sh
python -u scripts/validate_m0.py
./gradlew help :core:test :runtime:android:testDebugUnitTest :app:testDebugUnitTest
./gradlew :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest
git diff --check
```

LauncherVisualTest exercises seven destinations, captures top/bottom content and drawer,
checks visible text ellipsis, tests profile persistence via the new selector, and verifies
that malformed/unknown log records remain explicit. CI repeats it at the default phone
size, a compact window with 1.6x font scale, and landscape. These are emulator tests of
launcher UI, not validation of runtime execution or physical-device memory qualification.

## Review evidence

Pending final build, lint and screenshot inspection. Early CI runs exposed an implicit
style-parent resource and a raw-JSON row syntax error; both were corrected before visual
review. Final results and remaining limitations will be recorded after inspecting artifacts.
