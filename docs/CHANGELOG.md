# Changelog

trmtr-api's own versions, newest first. The number is the API's, separate from the mod's; `DEPRECATION.md` says what
each kind of version may change.

## 1.0.1

Packaging only: nothing a mod can see changes.

- **On Modrinth, one version for each loader family**: Fabric, Forge and NeoForge, each jar holding the same classes as
  the GitHub Maven's jar and only its own loader's metadata. The jar adds nothing to a game: on Fabric it is a library
  mod that Mod Menu badges as one, and on Forge and NeoForge it is a library the loader keeps apart from TRMT:
  Reimagined's own copy of the API. Players do not need it, as TRMT: Reimagined already includes these classes.
- **`maven.modrinth:trmtr-api:1.0.1`** resolves from this version on, beside `com.trmtgtnh:trmtr-api:1.0.1` on the
  GitHub Maven.

## 1.0.0

The first release.

- **The data format, format 1**: `families/`, `looks/`, `shapes/` and `wearers/` files and `plugin.json`, read from
  mod jars, resource packs, data packs (from 1.16.5) and the config folder (`FORMAT.md`).
- **The Java API, `com.trmtgtnh.api`**: plugins, wear listeners, ground and wearer rules, queries, actions and the
  render hook, the same in every edition and loader (`API.md`).
