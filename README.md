# Detail Armor Bar Reconstructed
[![](https://jitpack.io/v/coredex-source/DetailArmorBarReconstructed.svg)](https://jitpack.io/#coredex-source/DetailArmorBarReconstructed)

- Detail Armor Bar Reconstructed is a fork of a fork of the Detail Armor Bar mod created by [RedLime](https://github.com/RedLime). This fork will be maintained and will be worked upon constantly by [coredex-source](https://github.com/coredex-source).

# Features different from orignal mod:
- Enchantment overlay alignment (config option) - Align the overlay using levels or armor points.
- Per-piece enchantment coloring - When using aligned mode, each armor piece's enchantment shows its proper color (blast protection shows yellow, fire protection shows orange, etc.).
- Uniform color option - Apply a single customizable color to all enchantment effects regardless of protection type.
- Added animation speed to aura mode.
- Added none for enchantment overlay config options.
- Thorns overlay alignment - When using aligned mode, thorns overlay aligns with actual armor pieces.
- And many more..

# To-Do (for version 5.x.x):
- ~~Major change: Yarn -> Mojang mappings.~~
- ~~Add durability notifications.~~
- ~~Add armor trim overlay.~~
- ~~Add resource reload listener to refresh texture caches when resource packs change.~~
- Reformat the config screen.
- Rework the offset/positioning system.
- ~~Add inventory overlay for displaying armor protection type.~~
- ~~Add a durability overlay (will not be on the bar itself but somewhat like other armor hud mods unless I can pack it in there without making the bar too bloated).~~
- ~~Reformat and optimize existing code.~~
- Add a modern DABR API. (Will eventually replace the existing API)

# To-Do (for version 6.0.0):
- Add render support to the mod and the API that can dynamically fetch textures and colors to form a armor texture for modded and/or vanilla sets.
- Keep the png based texture loading system to maintain compatibility with existing resource packs and for the polished vanilla textures as dynamic loading will extract grayscale textures and repaint them which may or may not look unpolished.
- Load trim palettes dynamically from `textures/trims/color_palettes/<material>.png` for automatic mod compatibility.
- Extract armor material colors from `textures/models/armor/<material>_layer_1.png` for modded armor support.
- Hybrid approach: Use polished vanilla textures from armor_bar.png, fallback to dynamic extraction for unknown/modded armors.
- Add more in-built texture options for the armor bar.

# API:
- Developer docs: https://github.com/coredex-source/DetailArmorBarReconstructed/wiki/For-Developer-(API-Document)
- Should work with some changes to the mods which use the API.
eg. GildedArmor [CurseForge](https://www.curseforge.com/minecraft/mc-mods/gildedarmor) [Modrinth](https://modrinth.com/mod/gildedarmor)

# Modded armor set support:
- Will I add it sometime in the future? Yes, preferably a dynamic version.
- It already exists and is implemented in a brilliant way in [Detail Armor Bar Compat](https://modrinth.com/mod/detailab-compat)

# Downloads
- For 1.20.6 and below use the official mod by clicking here: [Modrinth](https://modrinth.com/mod/detail-armor-bar) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/detail-armor-bar)
- For 1.21 and above by clicking here: [Modrinth](https://modrinth.com/mod/detail-armor-bar-reconstructed) or [CurseForge](https://curseforge.com/minecraft/mc-mods/detail-armor-bar-reconstructed) or [Github-Releases](https://github.com/coredex-source/DetailArmorBarReconstructed/releases/latest)

# Version Support
- The mod will most likely only support the latest game verison and might support upto 3 game versions, for example if 26.1 is the latest Minecraft release then support might be provided for 1.21.10, 1.21.11 and will definitely be provided for 26.1.
- If a backport is to be requested, it can be requested be creating an issue [here](https://github.com/coredex-source/DetailArmorBarReconstructed/issues).
- Note: v5.3.0 will drop support for anything below 26.1. DO NOT ask for backports.

# Credits
- [RedLime](https://github.com/RedLime) for creating the [mod](https://github.com/redlime/DetailArmorBar).
- [rambert or ram6ert](https://github.com/ram6ert) for creating the [fork](https://github.com/ram6ert/DetailArmorBar) to 1.21.5.
- [Icedude907](https://github.com/Icedude907) for contributing to the code by fixing a visual bug.
- [axtrough](https://github.com/axtrough) for adding copper armor textures.

# Orignal Mod and Docs
- https://github.com/redlime/DetailArmorBar
- https://www.curseforge.com/minecraft/mc-mods/detail-armor-bar
- API Document : https://github.com/RedLime/DetailArmorBar/wiki/For-Developer-(API-Document)
