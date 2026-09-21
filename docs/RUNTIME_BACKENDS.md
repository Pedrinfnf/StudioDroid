# Runtime Backends

StudioDroid v2 should avoid being permanently tied to a single x86_64 translation backend

Planned abstraction

RuntimeBackend
├── FexBackend
└── Box64Backend

Each backend should expose a common interface for

- availability check
- installation
- configuration
- environment generation
- process launch
- process stop
- logs
- crash reporting
- feature detection

The Android application should communicate with the common runtime layer instead of directly depending on FEX or Box64
