# trmtr-api

The developer API of TRMT: Reimagined, the mod in which ground wears into paths where players and mobs walk. A mod
uses it to describe its own blocks and mobs to TRMT: Reimagined, to hear and steer wear, to read it, and to draw its
own worn blocks its own way.

Two halves, and most mods need only the first:

- **Data files**, no Java at all: which family a block wears as, how its worn ground is drawn, the shape it sinks
  into, and how hard a mob wears the ground. `docs/FORMAT.md` is the whole format.
- **The Java API**, the package `com.trmtgtnh.api`: a plugin hears and steers wear, reads it, and may draw a block of
  its own. `docs/API.md` is the whole surface.

The API is the same in every edition, from 1.7.10 to 26.3, is compiled at the Java 8 language level and names no class
of the game, so one jar serves every edition and loader.

## Getting started

Depend on the API at compile time only. The lines are the same for every loader:

```
repositories {
    maven { url "https://raw.githubusercontent.com/Xeptix/TRMTR-API/main/maven" }
}
dependencies {
    compileOnly "com.trmtgtnh:trmtr-api:1.0.1"
}
```

| Edition | Loaders | Notes |
|---|---|---|
| 1.7.10 | Forge | a plain `compileOnly`: the jar names no game class, so there is nothing to deobfuscate |
| 1.12.2 | Forge | as 1.7.10 |
| 1.16.5, 1.20.1 | Forge, Fabric | under Loom, `compileOnly` rather than `modCompileOnly`: the jar is not a mod and needs no remapping |
| 1.21.1, 26.1.2, 26.2, 26.3 | Forge, NeoForge, Fabric | as 1.16.5 |

Then:

1. Write a class implementing `com.trmtgtnh.api.TrmtrPlugin`.
2. Name it in `assets/<your namespace>/trmtr/plugin.json`, in your mod's jar:
   `{ "format": 1, "requires": "1.0", "class": "com.example.trmtr.ExamplePlugin" }`.
3. Put any data files beside it, in `assets/<your namespace>/trmtr/<kind>/`.

TRMT: Reimagined finds the plugin and the files in your jar on its own: there is no registration call and no event.
Without TRMT: Reimagined installed, the plugin is never loaded and no API class is touched, so a mod can support it
without requiring it. **Never shade the API into your jar**: TRMT: Reimagined carries these classes itself and is
their only provider.

## What is here

| Folder | What is in it |
|---|---|
| `docs/` | `API.md` (the Java surface, and the class each edition passes where the API says `Object`), `FORMAT.md` (the data format), `format-1.json` (the format as a JSON schema an editor can check files with), `DEPRECATION.md` (how versions change), `CHANGELOG.md`, and the surface record of each minor version |
| `src/` | the API's source |
| `example/` | an example mod that uses every part of the API: one plugin and one set of data files for every edition, and a small registration class per loader |
| `example-pack/` | an example resource pack and data pack, one folder per edition: looks for vanilla gravel and the example's blocks, stage pictures, and vanilla clay put into the dirt family |
| `maven/` | the Maven repository the Gradle lines above read: the jar, its sources and javadoc, its POM, and checksums of each |

## Licence

The API (this repository: its source, documents, example mod and example pack) is under the MIT License, in `LICENSE`.
TRMT: Reimagined itself is not: it stays under CC BY-NC 4.0.
