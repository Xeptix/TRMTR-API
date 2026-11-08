# The TRMTR data format (format 1)

A mod, a resource pack, a data pack or a player's config folder can tell TRMT: Reimagined which of its blocks wear
and how, without a line of Java. This document is every file and every key. The keys are also recorded as a JSON
schema, `format-1.json`, which an editor can use to check a file as it is written; the two say the same thing.

Format 1 is read by every TRMTR that carries trmtr-api 1.0.0 or later, in every edition from 1.7.10 to 26.3.

## Where files live

In a mod's jar:

```
assets/<namespace>/trmtr/families/<name>.json
assets/<namespace>/trmtr/looks/<name>.json
assets/<namespace>/trmtr/shapes/<name>.json
assets/<namespace>/trmtr/wearers/<name>.json
assets/<namespace>/trmtr/plugin.json
```

The same `trmtr/<kind>/` paths are read from packs and from the config folder (see "Sources and load order"). A
texture a file names is an ordinary texture of the game, so it travels in the jar or pack beside the file.

## Every file

```json families
{ "format": 1, "requires": "1.0", "_": ["free text, ignored"], "entries": [] }
```

### Keys: file

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `format` | integer | yes | `1` | The file format. A file without it, or with a higher one, is refused whole. |
| `requires` | string | no | major.minor | The lowest trmtr-api version the file needs, such as `1.0`. An older TRMTR skips the whole file with one line rather than half-reading it. |
| `_` | anything | no | | A note for whoever reads the file. Ignored. |
| `entries` | list of entries | yes | at most 4,096 | The entries, in the shape of the file's kind. |

Any other top-level key is warned about once and ignored. A file over 1 MB or with more than 4,096 entries is refused.

## Block selectors

Every `blocks` list (and `repairWith`) takes selectors in the edition's own spelling:

| Selector | Means | Editions |
|---|---|---|
| `ns:name` | every state of the block | all |
| `ns:name:3`, `ns:name:*` | one metadata value, or every one | 1.7.10, 1.12.2 |
| `ns:name[half=bottom]` | the states with these properties, the game's own syntax | 1.12.2 on, where the block declares them; see below |
| `#ns:tag` | every block in a block tag | 1.16.5 on |

A selector the edition cannot read, or a block that is not installed, is skipped quietly and counted in the summary
line. So a file meant for several editions names each edition's spelling side by side - `minecraft:grass` and
`minecraft:grass_block` - and each edition reads its own.

Properties select block states, and how many depends on the edition:

| Editions | What `[prop=value]` reaches |
|---|---|
| 1.7.10 | Nothing: the edition has no block states, and such a selector is skipped and counted. `ns:name:3` selects by metadata. |
| 1.12.2 | The states the block's sixteen metadata values stand for. A property the block does not keep in its metadata, such as a stair's `shape`, is worked out from its neighbours and cannot be selected. |
| 1.16.5, 1.20.1, 1.21.1, 26.1.2, 26.2, 26.3 | Every property of every state. `minecraft:oak_stairs[half=top]` is all forty states of that half, `[half=top,facing=north]` the eight of those facing north, and the rest of the block is untouched: `[type=bottom]` says nothing about a top slab. |

From 1.16.5 a selected state answers its entry everywhere the block would: its family, `resistance`, `cover`,
`wearsThroughTo`, what pays to mend it, its `look`, its wear pictures and its shape, on the server and, through the
server's table, on every client. An entry about the whole block and one about some of its states merge key by key,
as any two entries do. A `shapes` entry's `state` selects the same way. In `repairWith` a property means nothing,
because what pays is an item: `ns:name[prop=value]` is paid by `ns:name`. Wear pictures are kept per block in sixteen
places, the same number the metadata editions have, so one block shows at most sixteen different looks and families
across its states; past that its other states draw the block's own, and the log says so once.

Entity selectors (in `entities`) are entity ids in the edition's own spelling - `Zombie` or `modid.Name` on 1.7.10,
`ns:name` from 1.12.2 - or `#ns:tag` where the edition has entity tags.

## `families/` - which family a block wears as

Server data: the server decides, and a visiting client follows the server.

```json families
{ "format": 1, "entries": [
  { "blocks": ["example:mossy_loam"], "family": "grass", "repairWith": ["example:loam"] },
  { "blocks": ["example:loam", "example:loam_ledge"], "family": "dirt", "resistance": 1.0 },
  { "blocks": ["example:packed_loam"], "family": "dirt", "resistance": 4.0 },
  { "blocks": ["example:loam_sprout"], "family": "vegetation", "cover": "breaks" },
  { "blocks": ["example:glass_gravel"], "family": "none" },
  { "blocks": ["example:loam_road"], "family": "cobble", "wearsThroughTo": ["gravel", "dirt"] }
] }
```

### Keys: families

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `blocks` | list of block selectors | yes | | The blocks this entry is about. |
| `family` | string | no | `grass`, `dirt`, `sand`, `gravel`, `stone`, `cobble`, `leaves`, `vegetation`, `nether`, `end`, `snow`, `ice`, `none` | The family the blocks wear as. `none` stands the blocks aside: they never wear and are never painted, whatever detection would have said. |
| `resistance` | number | no | 1.0 to 64.0 | How many times more traffic the blocks take before they advance a stage. Healing is unaffected. |
| `repairWith` | list of block selectors | no | | Blocks a tamper accepts as payment for mending them, besides a block of themselves. |
| `cover` | string | no | `breaks`, `holds` | For ground cover: whether walking breaks it or it holds. |
| `wearsThroughTo` | list of strings | no | `grass`, `dirt`, `sand`, `gravel`, `stone`, `cobble`, `nether`, `end`, `snow`, `ice`, `none` | The block's own chain: the surfaces it shows as it wears through, in the order the ground reaches them. `none`, alone, keeps the block sinking as itself whatever its family wears through to. Without it the block follows its family's chain. |

A file names the built-in families only. A file cannot make a new family: the family a block wears as is stored in
every worn record of every world, so a new one is a change to TRMTR itself.

A block's own chain is its family's in everything but what the ground shows as it sinks: the same run on its face,
the same depth, the same number of steps and the same prices and heal times. It follows the player's settings as the
family's list does, so with wearing through to other surfaces switched off the block sinks as itself, and a family
switched off is passed over. The list belongs to the family it was written for: the entry's own `family`, or the
family TRMTR finds for the block when the entry gives none. A block the player's own lists put in another family
follows that family's chain. `none` beside a family is refused. Changing a block's list never loses its wear: a worn
square keeps its depth and is shown as the new chain shows that depth, and a server sends each block's chain to its
visitors, so a player without the file sees the server's.

## `looks/` - how worn ground is drawn

Client data: each player's game draws what its own packs and mods say.

```json looks
{ "format": 1, "entries": [
  { "blocks": ["example:mossy_loam"], "look": "grass", "strength": 0.8 },
  { "blocks": ["example:loam"], "look": "dirt_lite", "face": "example:loam_top" },
  { "blocks": ["example:glowing_loam"], "look": "dirt", "innerLayer": "minecraft:lava_still" },
  { "blocks": ["example:runestone"], "stages": ["example:trmtr/runestone_0", "example:trmtr/runestone_1",
                                               "example:trmtr/runestone_2", "example:trmtr/runestone_3"] }
] }
```

Texture names are the edition's own: an icon name on 1.7.10 (`example:loam_top`), a sprite path from 1.12.2
(`example:blocks/loam_top` on 1.12.2, `example:block/loam_top` from 1.16.5). The example above is spelt for 1.7.10.

### Keys: looks

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `blocks` | list of block selectors | yes | | The blocks this entry is about. |
| `look` | string | no | `grass`, `dirt`, `sand`, `polish`, `crack`, `smooth`, `crack_lite`, `smooth_heavy`, `dirt_lite`, `smooth_crack`, `smooth_dirt`, `crack_dirt`, `crack_dirt_lite` | The wear look, worked from the block's own pixels, by its id. The ids never change once they ship. |
| `strength` | number | no | 0.0 to 1.0 | How strongly the look is applied. Lower leaves more of the block's own character showing. |
| `face` | texture name | no | | The texture to wear in place of the block's own top. |
| `innerLayer` | texture name | no | | What shows behind a face that is cut away, such as lava behind a cracked lava stone. |
| `stages` | list of texture names | no | 2 to 16 pictures | Pictures drawn by hand, which replace the generated ones. TRMTR spreads its wear stages across them in order. Each is square, at the block's own size. |

## `shapes/` - the shape wear is drawn on and sinks into

Server data, sent to visiting clients with the rest of the server's table, because a shape decides where a player
stands.

```json shapes
{ "format": 1, "entries": [
  { "blocks": ["example:loam_ledge"], "state": { "type": "bottom" }, "meta": 0, "shape": "slab" },
  { "blocks": ["example:loam_step"], "shape": "boxes", "boxes": [[0, 0, 0, 16, 12, 16]], "maxSinkPixels": 4 },
  { "blocks": ["example:runestone"], "shape": "full", "sinks": false }
] }
```

Without an entry, a stair block is a stair, a half-height slab a slab, and everything else full.

Once the ground at a block is worn, collision, the outline and the drawing follow its shape on the server and on
every client: with `boxes`, each box is drawn and collided with on its own, and only the walked boxes sink. TRMTR takes
the boxes as the block's own shape, so give the boxes the block collides with. Unworn, the block answers for itself.

### Keys: shapes

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `blocks` | list of block selectors | yes | | The blocks this entry is about. |
| `shape` | string | yes | `full`, `slab`, `stair`, `boxes` | The shape. With `boxes`, the boxes are given below. |
| `state` | object | no | | The block-state properties this entry is for, such as `{ "type": "bottom" }`. Read by editions with block states. |
| `meta` | integer | no | 0 to 15 | The metadata value this entry is for. Read by 1.7.10 and 1.12.2. An entry may carry both `state` and `meta`. |
| `boxes` | list of boxes | no | 1 to 8 boxes | Required with `boxes`. Each box is `[x0, y0, z0, x1, y1, z1]` in pixels, 0 to 16; the box with the highest top is the walked face. |
| `maxSinkPixels` | integer | no | 0 to 15 | How deep the wear may sink, in pixels. Held below the walked box's height. |
| `sinks` | boolean | no | | `false` keeps the wear flat on the face. |

## `wearers/` - what wears the ground, and how hard

Server data.

```json wearers
{ "format": 1, "entries": [
  { "entities": ["example:mole"], "multiplier": 0.5 },
  { "entities": ["example:wisp"], "multiplier": 0 },
  { "entities": ["example:pack_yak"], "group": "passive" }
] }
```

### Keys: wearers

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `entities` | list of entity selectors | yes | | The mobs this entry is about. |
| `multiplier` | number | no | 0.0 to 4.0 | How hard the mob wears the ground, as a share of a player's step. 0 keeps it off the ground. |
| `group` | string | no | `hostile`, `passive`, `villager` | The group the mob counts in, for the player's group switches and multipliers. |

The player's own mob list in the TRMTR settings still wins over any file, and a 0 there still keeps a mob off the
ground.

## `plugin.json` - the doorway to Java

Read from mod jars only, at `assets/<namespace>/trmtr/plugin.json`. A `plugin.json` in a pack or the config folder is
refused, because a pack cannot carry code. `API.md` covers what the class does.

```json plugin
{ "format": 1, "requires": "1.0", "class": "com.example.trmtr.ExamplePlugin" }
```

### Keys: plugin

| Key | Type | Required | Values | What it does |
|---|---|---|---|---|
| `format` | integer | yes | `1` | The file format. |
| `requires` | string | no | major.minor | The lowest trmtr-api version the plugin needs. |
| `_` | anything | no | | A note. Ignored. |
| `class` | string | yes | a class name | The plugin's class, which implements `com.trmtgtnh.api.TrmtrPlugin` and has a public constructor without arguments. |

## Sources and load order

TRMTR reads data in layers. A higher layer overrides a lower one.

| Layer (low to high) | Where | Kinds read | Editions |
|---|---|---|---|
| 0. Built in | TRMTR's own defaults and detection | - | all |
| 1. Mod jars | `assets/<ns>/trmtr/` in each mod's file, on both sides | all five | all |
| 2. Packs | resource packs: `assets/<ns>/trmtr/looks/`; data packs: `data/<ns>/trmtr/families/`, `shapes/` and `wearers/` | as listed | resource packs in all; data packs from 1.16.5 |
| 3. Config folder | `config/trmtr/<ns>/<kind>/*.json` | all but `plugin.json` | all |
| 4. The player's settings | the TRMTR config's own lists and per-family settings | - | all |

- Inside a layer, mod jars are read by namespace and then path, packs in the game's own pack order (the later pack
  wins, as it does for the game's own resources), and config files by path. A plugin's `Registrar.data` call counts
  as its own mod's jar.
- A later entry overrides an earlier one key by key, not file by file: a pack that sets only a block's `look` keeps
  the `strength` the mod gave it.
- In the mod-jar layer, the owner wins its own blocks: an entry from namespace `example` about an `example:` block
  beats any other jar's entry about the same block, whatever the order.
- Data beats detection, and the player beats data. A player's exclusions, a family switched off, the per-family block
  lists and the mob list all sit in layer 4. Data is never written into the player's config, so removing a mod leaves
  nothing of it behind.
- Families, shapes and wearers are the server's; looks are the client's. A resource pack's `families/`, `shapes/` or
  `wearers/` folder, and a data pack's `looks/`, are ignored with one line, because a resource pack deciding where a
  player stands would put a client at odds with its server.
- A client whose mods or packs give it other data than the server's uses the server's table for the visit.
- Data is read when the game starts and again on `/trmt reload`; from 1.16.5, server data also on every data-pack
  reload (`/reload` included), and looks on every resource reload.

## When a file is wrong

A bad file never stops the game.

- A file that does not parse is refused whole, with its line and column. A missing or higher `format` refuses the
  whole file too.
- An entry with a wrong type, a number out of range, or a family or look that does not exist is refused alone; the rest
  of the file is kept.
- A `plugin.json` anywhere but a mod jar is refused, and so is a render hook for another namespace's block.
- A block or entity that is not installed is not an error: it is skipped, and counted.
- Each problem is said once per session, at WARN, naming the file, where it came from and whom to tell, for example:
  `TRMTR data: refused assets/example/trmtr/families/loam.json in example-1.0.jar (mod example) - line 4, column 17:
  expected ',' - report this to that mod`.
- Each read ends with one summary line at INFO: how many files, from how many mods and packs, how many blocks, shapes,
  wearers and plugins, how many files were refused and how many names were not installed.
- `/trmt data` shows the sources read, the counts, every refusal still standing, and, for a named block, where each of
  its values came from. A name with properties, such as `/trmt data minecraft:oak_stairs[half=top]`, leaves out the
  selectors of other values, and from 1.16.5 the answer says how many of the block's states answer for themselves.

## How the format changes

`format` changes only with a major trmtr-api version. A minor version may add optional keys and new kinds; an older
TRMTR warns once about the key it does not know and goes on, or skips the file if its `requires` says it must. A key
that is replaced keeps being read, marked deprecated, for every later minor of that major, and its use is logged once
per file with the key that replaces it. `DEPRECATION.md` is the whole policy.
