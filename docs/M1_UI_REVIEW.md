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
size, a compact window with 1.6x font scale, landscape and a tablet-sized window.
LogViewerUiTest also opens the raw JSON dialog and verifies the bounded clipboard copy.
The workflow requires 60 non-empty screen captures and the raw-log dialog capture. These are emulator tests of
launcher UI, not validation of runtime execution or physical-device memory qualification.

## Review evidence

Three real visual review rounds informed the implementation. The first Home screenshot showed an
oversized hero and a tinted logo that hid its internal strokes. The second set of 45
screenshots showed that logs and wide layouts could be denser; those were refined, while
Diagnostics now labels the primary advertised ABI instead of inferring native CPU
architecture from presence of ARM64 support in the emulator's ABI list. Profile cards,
compact readiness rows, metrics, neutral Unknown badges, drawer and future presentation
path were inspected on actual emulator screenshots.

The first screenshot transfer failed despite passing UI assertions; the test writer now
uses MediaStore and CI asserts the expected image names and non-empty sizes. Missing
captures fail validation. Early build/lint findings were fixed; the programmatic-only view
constructor lint warning has a narrow documented suppression, with no error baseline.

The complete intermediate [QA run 35757997632](https://github.com/Pedrinfnf/StudioDroid/actions/runs/35757997632)
passed build, unit tests, UI tests, lint and capture assertions (45 images at that revision).
Lint retained the baseline 20 warnings with zero errors. A fresh-install idle sample on the
same emulator measured 58,653 KiB PSS for the previous M1 and 62,150 KiB for the redesigned
launcher: +3,497 KiB, about 3.4 MiB (+6%). This is one controlled sample, not a memory
benchmark or physical-device qualification. The intermediate APK grew by about 123 KB.

## Final validation and delivery

The final [QA run 35761693565](https://github.com/Pedrinfnf/StudioDroid/actions/runs/35761693565)
passed on source `ee1d7af162996fb2c789def266e8f20c76d04a4f`. Only review documentation
was changed afterward. The application source in the delivered APK matches that revision.

| Gate | Result |
| --- | --- |
| M0/M0.1 | Four schema meta-validations; 416 fixtures plus four valid bases; 12 strict JSON documents; nine duplicate/numeric cases; 49 ECMAScript patterns and 87 assertions passed |
| Final local documentation checks | 60 local Markdown links, metadata/license checks and `git diff --check` passed |
| Gradle | Configuration, debug APK and instrumentation APK builds passed with the pinned M1 toolchain |
| JVM tests | 20 passed: DeviceProfilePolicy 6, MemoryPolicy 4, LaunchPlanner 8, RotatingLogStore 2; app JVM task has no sources |
| Android tests | Five distinct tests passed; 14 total executions across phone, 1.6x font, landscape and tablet configurations |
| Lifecycle and persistence | Existing recreation/controller test passed; profile selector persists across recreation and returns to Automatic |
| Logs | Structured parsing preserves Unknown; raw dialog display and bounded clipboard copy passed |
| Lint | Zero errors, 19 warnings; remaining warnings concern pinned versions/target SDK, ARM64-only ChromeOS support and existing backup configuration |
| Captures | 61 non-empty PNGs verified: 60 page/drawer captures plus the raw-log dialog |
| APK integrity | SHA-256 checked locally and official apksigner verified the v2 debug signature |

Final visual inspection covered all seven phone destinations, the drawer and raw dialog;
Home/Runtime/Diagnostics bottom content; large-font Diagnostics/Settings/Logs/drawer;
landscape Home/Settings; and tablet Home/Diagnostics/Settings. These 21 inspected captures
show wrapping labels, consistent blue surfaces, neutral Unknown states, compact connected
runtime rows and responsive profile/metric layouts. Automated ellipsis assertions cover
the visible top/bottom content in all four configurations; they are not a complete proof
against every possible clipping case, font scale or device window.

- [QA APK: StudioDroid-M1-debug-14](https://github.com/Pedrinfnf/StudioDroid/actions/runs/35761693565/artifacts/10710069403)
- [Reports and screenshots: M1-validation-14](https://github.com/Pedrinfnf/StudioDroid/actions/runs/35761693565/artifacts/10710856797)
- [Exact created/modified source file inventory](M1_UI_FILES.md)

APK size: 14,691,410 bytes. SHA-256:

```text
5c2bb91272247d3fbd35e1ee5006b47b803652211ab0bc5c4993a2982867f78c
```

Artifacts have 14-day retention. Debug signing keys are ephemeral between CI runners;
a previous preview may need removal before this APK can be installed. Removing it also
removes its private settings/logs. No release or main-branch merge was created.

The final fresh-install idle comparison used the same API 35 emulator and baseline APK
from run `35674215423`: previous M1 PSS 58,361 KiB; current PSS 61,988 KiB; delta 3,627 KiB
(3.54 MiB, 6.21%). One controlled sample cannot establish a distribution or qualify a
physical 4 GB device. It does show that this UI revision did not add a large resident cost
in the measured scenario. No runtime was installed or running during the comparison.

## Validation boundaries

Local Termux validation covered M0/M0.1, documentation, resource XML, source-scope diff,
APK checksum/signature and artifact review. The new APK build, lint, JVM tests and Android
instrumentation results above come from GitHub's x86_64 build host. The local Gradle test
attempt did not produce a completed result, so it is not counted as a pass. Google's x86_64
Android tools were not replaced, patched or downgraded for the ARM64 host.

UI instrumentation ran on an API 35 emulator. API 29/36 device behavior, physical 4 GB
memory pressure, full TalkBack exploration and motion smoothness on hardware remain
unqualified. NativeSurfaceBackend and its transport remain future work; this validation
exercises launcher UI only. Core/runtime/NativeBridge, the existing ViewModel, schemas and
M0 harness have no source changes relative to `f229a3a`. M2 has not started.
