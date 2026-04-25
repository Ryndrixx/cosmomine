# CosmoMine

![CosmoMine](src/main/resources/cosmomine.png)

A custom veinminer mod for the **Blockbusters Aeronautics** modpack. Built for NeoForge 1.21.1.

---

## How to Use

| Action | How |
|--------|-----|
| Veinmine | Hold **~** while mining a block |
| Cycle shape | Scroll mouse wheel **while holding ~** |
| Mass plant seeds | Hold a seed + hold **~** + right-click farmland |
| Mass place blocks | Hold a block + hold **~** + right-click a surface |

The current shape mode appears in your action bar when you scroll.

Mass planting flood-fills all connected farmland with seeds. Mass placing fills a 3×3 grid on the clicked surface.

---

## Shape Modes

| Shape | Description |
|-------|-------------|
| **Vein** | Flood-fills all connected blocks of the same type (default) |
| **Tunnel 1×2** | 1 wide × 2 tall tunnel in the direction you're facing |
| **Tunnel 3×3** | 3×3 tunnel in the direction you're facing |
| **Flat 3×3** | 3×3 plane perpendicular to your facing direction |
| **Mine Staircase** | Descending staircase in your facing direction |
| **Escape Tunnel** | Ascending staircase in your facing direction |

Cyan outlines preview which blocks will be mined **before** you break them.

---

## Config

Settings can be changed **in-game** via the Mods list — click the config button next to CosmoMine. No file editing required.

They are also stored at `config/cosmomine-common.toml` if you prefer to edit directly.

| Setting | Default | Description |
|---------|---------|-------------|
| `maxBlocks` | 64 | Max blocks per veinmine operation (1–256) |
| `requireCorrectTool` | true | Only veinmine blocks your current tool can harvest |
| `consumeHunger` | false | Each extra block costs a small amount of hunger |
| `requireSneakToCycle` | false | Require holding Sneak (shift) + **~** to scroll-cycle shape modes |
| `color` | `#00BFFF` | Outline highlight color (hex) |
| `opacity` | 0.9 | Outline opacity (0.0–1.0) |
| `lineWidth` | 1.5 | Outline line width in pixels (0.5–8.0) |

---

## Installation

Drop `cosmomine-0.6.1.jar` into your `mods/` folder alongside NeoForge 1.21.1.

---

## Building from Source

```bash
git clone https://github.com/Ryndrixx/cosmomine
cd cosmomine
./gradlew build
# Output: build/libs/cosmomine-0.6.1.jar
```

Requires Java 21.

---

*Built by Cosmo for the Blockbusters Aeronautics modpack.*
