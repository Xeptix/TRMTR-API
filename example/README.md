# The TRMTR API example mod

A small mod that uses everything trmtr-api offers, written as any mod would write it: it compiles against
`com.trmtgtnh:trmtr-api` and names nothing else of TRMT: Reimagined.

## What it adds

- **Four blocks**, described to TRMTR by data alone (`common/resources/assets/example/trmtr/`):
  - `example:mossy_loam` wears as grass and is drawn with the `grass` look; a tamper mends it with loam.
  - `example:loam` wears as dirt.
  - `example:loam_step` is 12 pixels tall: its `shapes/` entry gives TRMTR that box, and wear sinks into it at most
    4 pixels.
  - `example:runestone` wears as stone, takes twice the traffic, and keeps its wear flat.
- **The Wayfarer's Charm** (`example:charm`): anything holding it wears the ground half as hard.
- **A plugin** (`common/java/com/example/trmtr/ExamplePlugin.java`, named in `plugin.json`):
  - a `WearerRule` halving a mob's wear while it holds the charm;
  - a `WearListener` that halves a player's wear the same way (a wearer rule weighs mobs, never a player's step) and
    writes a line to the log for every change of wear on the example's blocks;
  - a `WearRenderer` for the runestone, which inlays a rune in its worn top, a pixel deeper with each stage.
- **`/examplewear`**: the wear under the player, or at `x y z`, read through `Queries`.
  `/examplewear hook off` hands the runestone back to TRMTR, which then draws the stage pictures a pack gives it;
  `/examplewear hook on` takes it back. The switch is the drawing game's own, so it is for single player.

## How it is laid out

| Folder | What is in it |
|---|---|
| `common/java/` | The plugin, the same file for every edition and loader. It uses trmtr-api and Java alone. |
| `common/resources/` | `plugin.json` and the data files, the same for every edition: they name blocks and look ids only, never a texture, since texture names are spelt per edition. |
| `<edition>/<loader>/` | One small class per loader that registers the blocks and the charm, gives the plugin the answers only that edition has (`Example.Edition`), and adds the command; with its `mcmod.info` or loader metadata and its language file. |

The textures are the game's own, so the example carries no art: mossy loam is grass's top tinted moss green, loam
and the step are dirt, and the runestone is chiselled stone brick.

## Building it into a mod of your own

Depend on the API at compile time only, put the plugin and the data in your jar, and name the plugin in
`assets/<your namespace>/trmtr/plugin.json`. `API.md` and `FORMAT.md` say the rest.
