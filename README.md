<div align="center">

# ⚔️ AttackIndicator

**Lightweight, version-adaptive floating damage indicators for Paper & Spigot servers.**

Beautiful animated damage numbers that rise above any entity the moment it takes damage —
rendered with the best technology your server version supports, automatically.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.8%20%E2%86%92%201.21+-brightgreen?logo=minecraft)](https://modrinth.com/plugin/attack-indicator)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-8+-orange?logo=openjdk)](https://adoptium.net/)
[![Modrinth](https://img.shields.io/badge/Download-Modrinth-00AF5C?logo=modrinth)](https://modrinth.com/plugin/attack-indicator)
[![bStats](https://img.shields.io/badge/Stats-bStats-2C2D72)](https://bstats.org/plugin/bukkit/Attack%20indicator)

</div>

---

## ✨ Overview

AttackIndicator hooks into the server's damage pipeline and spawns a short-lived,
self-animating text entity above the victim, displaying the amount of damage dealt.
It is built around a single principle: **one plugin, every version** — the rendering
engine is selected at runtime so the same JAR runs on a 1.8 PvP server and a modern
1.21 SMP without configuration changes.

## 🚀 Version Support

The plugin detects available server APIs at startup and picks the optimal renderer:

| Server Version    | Renderer            | Text Engine                       | Notes                                    |
| ----------------- | ------------------- | --------------------------------- | ---------------------------------------- |
| **1.19.4+**       | Display Entities    | Adventure / MiniMessage           | Smoothest, scalable, lowest overhead     |
| **1.16.5 – 1.19.3** | Armor Stands        | Adventure / MiniMessage           | Full hex colors & gradients              |
| **1.8 – 1.16.4**  | Armor Stands        | Legacy `ChatColor` (`&` / `<tag>`) | Basic 16 colors; hex/gradients stripped  |

Detection is purely capability-based (via reflection on `org.bukkit.entity.Display`
and the Adventure `MiniMessage` class), so forks and hybrid servers are handled
gracefully, with an automatic fallback to the legacy renderer if anything fails to load.

## 🎯 Features

- **Adaptive rendering** — Display Entities or Armor Stands chosen automatically.
- **Rich text** — MiniMessage hex colors, gradients and styles on 1.16.5+; graceful
  downgrade to legacy color codes on older versions.
- **Smooth animation** — configurable upward speed and lifetime.
- **Source filtering** — show indicators for `ALL`, `PLAYER_ONLY`, or `NO_SELF` damage.
- **Entity filtering** — whitelist or blacklist specific `EntityType`s.
- **Player damage** — optionally show indicators when players take damage.
- **World control** — disable indicators per world.
- **Per-player toggle** — players can turn indicators on/off for themselves; persisted to disk.
- **Performance cap** — hard limit on concurrent indicators to protect TPS during burst damage.
- **Crash-safe** — on 1.13+ indicators are transient entities (`setPersistent(false)`), so a
  crash or chunk unload never leaves orphaned displays/armor stands behind. (The persistence
  API is unavailable on 1.8–1.12, where this safeguard degrades gracefully.)
- **Update notifications** — checks Modrinth asynchronously on startup.
- **i18n** — English and Russian bundled; add your own `lang/<code>.yml`.

## 📦 Installation

1. Download `AttackIndicator-x.y.jar` from [Modrinth](https://modrinth.com/plugin/attack-indicator)
   or the [Releases](https://github.com/Eneryleen/Attack-indicator/releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart the server (or run a plugin manager reload).
4. Edit `plugins/AttackIndicator/config.yml` and run `/ai reload`.

## 🕹️ Commands

| Command             | Description                                | Permission              |
| ------------------- | ------------------------------------------ | ----------------------- |
| `/ai reload`        | Reload `config.yml` and language files     | `attackindicator.reload` |
| `/ai toggle`        | Toggle indicators on/off for yourself      | `attackindicator.toggle` |

Aliases: `/attackindicator`, `/ai`.

## 🔐 Permissions

| Permission                | Description                              | Default |
| ------------------------- | ---------------------------------------- | ------- |
| `attackindicator.reload`  | Allows reloading the configuration       | `op`    |
| `attackindicator.toggle`  | Allows toggling indicators for oneself   | `true`  |

## ⚙️ Configuration

Full reference (see `config.yml` for inline documentation):

```yaml
# Language file to load from /lang (en, ru, or your own)
language: en

# Indicator text. {damage} is replaced with the dealt amount.
# MiniMessage on 1.16.5+; hex/gradients are stripped on older versions.
indicator-format: "<#ffffff>-{damage}<#ff5555>♥"

# Animation
display-duration: 40      # lifetime in ticks (20 ticks = 1s)
upward-speed: 0.03        # blocks per tick the indicator floats up
vertical-offset: -0.5     # offset from the entity's head, in blocks

# Random jitter so stacked hits don't overlap (total spread per axis, in blocks)
random-offset:
  enabled: true
  x: 0.5
  y: 0.5
  z: 0.5

# Which damage to show: ALL | PLAYER_ONLY | NO_SELF
display-mode: PLAYER_ONLY

# Disable indicators in these worlds
disabled-worlds:
  - world_nether_example

# Show indicators when players take damage
show-on-players: false

# Display Entity scale (1.19.4+ only; ignored on armor-stand versions)
indicator-scale: 1.5

# Performance guard
performance:
  # Max concurrent indicators; 0 = unlimited. Bounds entity & tick load on bursts.
  max-active-indicators: 200

# Show/hide indicators per entity type
entity-filter:
  whitelist-mode: false   # true = only listed types; false = all except listed
  entities: []            # e.g. [ZOMBIE, SKELETON]
```

### Formatting examples

```yaml
indicator-format: "<#ffffff>-{damage}<#ff5555>♥"               # hex + heart (1.16.5+)
indicator-format: "<gradient:#ff0000:#ffff00>-{damage}</gradient>"  # gradient (1.16.5+)
indicator-format: "&f-{damage}&c♥"                             # legacy codes (1.8+)
```

## 🧱 Architecture

```
AttackIndicator (JavaPlugin)
├── ConfigManager           — typed access to config.yml
├── LangManager             — i18n message loading & placeholder substitution
├── PlayerToggleManager     — per-player on/off state, persisted to disabled_players.yml
├── DamageListener          — filters damage events, resolves the attacker
├── ReloadCommand           — /ai reload & /ai toggle (+ tab completion)
├── UpdateChecker           — async Modrinth version check
└── indicator/
    ├── IndicatorSpawner     — renderer interface (spawn + cleanup)
    ├── IndicatorFactory     — capability detection → picks a renderer at runtime
    ├── modern/ModernIndicatorManager   — TextDisplay (1.19.4+), Adventure
    └── legacy/
        ├── LegacyIndicatorManager      — ArmorStand fallback
        └── TextFormatter               — MiniMessage→legacy reflection bridge
```

The renderer is loaded reflectively by `IndicatorFactory`, so classes that reference
version-specific APIs (e.g. `Display`, `joml`) are never loaded on servers that lack them.

## 🔨 Building from source

Building requires **JDK 17+** and Maven — the Paper 1.19.4 API artifact is compiled
as Java class-file 61. The plugin itself targets **Java 8 bytecode** (`pom.xml`
`java.version=8`), so the resulting JAR runs on Java 8 servers all the way up to modern ones.

```bash
git clone https://github.com/Eneryleen/Attack-indicator.git
cd Attack-indicator
mvn clean package
```

The shaded, ready-to-use JAR is produced in `target/attack-indicator-<version>.jar`.
Dependencies (`bStats`, `gson`) are relocated under the plugin package to avoid conflicts.

## 📊 Statistics

[![bStats Graph](https://bstats.org/signatures/bukkit/Attack%20indicator.svg)](https://bstats.org/plugin/bukkit/Attack%20indicator)

## 📄 License

Released by **[Eneryleen](https://github.com/Eneryleen)**. See the repository for license details.
