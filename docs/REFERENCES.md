# Research references and attribution

Reviewed during M0 on 2026-09-20. Revisions below identify inspected source, not tested
runtime releases. See [Vodka findings](research/VODKA.md),
[ZalithLauncher2 findings](research/ZALITHLAUNCHER2.md), and [NOTICE](../NOTICE).

| Reference | Inspected revision | Use |
| --- | --- | --- |
| [Vodka](https://github.com/Shellworks-Development/vodka/tree/ca41f2b074fb82a5dc203411c234ebf18b455d9c) | ca41f2b074fb82a5dc203411c234ebf18b455d9c | Primary runtime architecture research; experimental, not a proven product |
| [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2/tree/1bef4ab9c49ee98db46c4fa39a6ea7b74dfac0b3) | 1bef4ab9c49ee98db46c4fa39a6ea7b74dfac0b3 | Renderer registry, plugin metadata, profiles, components and recovery concepts only |
| [FEX](https://github.com/FEX-Emu/FEX/tree/48d71752e2a363d5535181f803b0b0e9666c1825) | 48d71752e2a363d5535181f803b0b0e9666c1825 | Rootfs/thunk/helper constraints; initial source candidate, not qualified |
| [Termux:X11](https://github.com/termux/termux-x11/tree/bd1cfadd74f98b548662a48b6e1d564aa434c86e) | bd1cfadd74f98b548662a48b6e1d564aa434c86e | Research-only buffer/synchronization evidence; no app/runtime dependency |

## Primary technical documentation

- [Android modern-target execution restrictions](https://developer.android.com/about/versions/10/behavior-changes-10)
- [Android Vulkan architecture](https://source.android.com/docs/core/graphics/arch-vulkan)
- [Android memory callbacks](https://developer.android.com/reference/android/content/ComponentCallbacks2)
- [Android 16 KiB page-size support](https://developer.android.com/guide/practices/page-sizes)
- [AGP 8.13 compatibility](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [FEX rootfs guide](https://wiki.fex-emu.com/index.php/Development:Setting_up_RootFS)
- [FEX thunk guide](https://github.com/FEX-Emu/FEX/blob/48d71752e2a363d5535181f803b0b0e9666c1825/ThunkLibs/README.md)
- [FEX helper lifecycle](https://github.com/FEX-Emu/FEX/blob/48d71752e2a363d5535181f803b0b0e9666c1825/Source/Common/FEXServerClient.cpp)
- [DXVK driver requirements](https://github.com/doitsujin/dxvk/wiki/Driver-support)
- [Wine Wayland implementation](https://github.com/wine-mirror/wine/tree/master/dlls/winewayland.drv)
- [Mesa Venus requirements](https://docs.mesa3d.org/drivers/venus.html)
- [libadrenotools](https://github.com/bylaws/libadrenotools): Adreno-specific research, not a universal driver solution
- [Official WindowsStudio64 metadata](https://clientsettingscdn.roblox.com/v2/client-version/WindowsStudio64)
- [GNU GPL guidance](https://www.gnu.org/licenses/gpl-faq.html)
- [Wine license](https://github.com/wine-mirror/wine/blob/master/LICENSE)
- [DXVK license](https://github.com/doitsujin/dxvk/blob/master/LICENSE)

Live docs and master links are discovery references, not build inputs. Freeze relevant
source/toolchain revisions and requirement evidence before implementing a component.
GPL text in LICENSE is the SPDX GPL-3.0-only text, obtained from
[SPDX license data](https://github.com/spdx/license-list-data/blob/main/text/GPL-3.0-only.txt);
its SHA-256 is fb981668c18a279e285fc4d83fba1e836cc84dd4daa73c9697d3cfd2d8aca6e0.

## Reuse policy

Do not blindly copy Vodka or Minecraft-specific systems. StudioDroid-owned work uses
GPL-3.0-only; upstream files keep their actual grants. Record provenance for copied or
derivative source, preserve notices, and satisfy corresponding-source obligations before
distribution. Reference repositories are not payload mirrors. Termux:X11 is research-only.

## Excluded projects

baldbuffalo/Roblox-Studio-on-Android must not be used as a source or reference.
The previous RobloxDroid / StudioDroid implementation is intentionally not treated as a
reference: v2 is a fresh second-generation architecture.
