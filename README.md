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

Located at `config/cosmomine-common.toml` in your Minecraft instance folder.

| Setting | Default | Description |
|---------|---------|-------------|
| `maxBlocks` | 64 | Max blocks per veinmine operation (1–256) |
| `requireCorrectTool` | true | Only veinmine blocks your current tool can harvest |
| `consumeHunger` | false | Each extra block costs a small amount of hunger |

---

## Installation

Drop `cosmomine-0.2.0.jar` into your `mods/` folder alongside NeoForge 1.21.1.

---

## Building from Source

```bash
git clone https://github.com/Ryndrixx/cosmomine
cd cosmomine
./gradlew build
# Output: build/libs/cosmomine-0.2.0.jar
```

Requires Java 21.

---

*Built by Cosmo for the Blockbusters Aeronautics modpack.*
