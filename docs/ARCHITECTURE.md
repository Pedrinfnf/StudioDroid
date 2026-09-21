# StudioDroid v2 Architecture

## Goal

Run the real Roblox Studio Windows x86_64 executable on Android ARM64

## Planned stack

Android App
↓
Runtime Controller
↓
Linux / compatibility environment
↓
x86_64 translation backend
↓
Wine
↓
DXVK / Vulkan
↓
Roblox Studio

## Runtime backends to research

- FEX-Emu
- Box64

## Core principles

- clean second-generation architecture
- no dependency on the previous StudioDroid / RobloxDroid codebase
- Android ARM64 first
- runtime abstraction
- backend swapping without rewriting the whole app
- reproducible builds
- strong crash logging and diagnostics
- clean separation between Android UI and runtime
- research before integration
- avoid unnecessary compatibility shims
- prefer upstream solutions over local hacks when possible

## Planned modules

### app
Android UI
settings
installer flow
runtime controls
logs
device compatibility checks

### runtime
runtime backend abstraction
Wine environment
FEX / Box64 integration
DXVK
Vulkan
rootfs management
Studio launcher

### native
JNI
Android native APIs
surface / window integration
low-level runtime bridges

### scripts
build scripts
runtime payload generation
dependency fetchers
CI helpers

### docs
architecture
research
runtime decisions
compatibility notes
