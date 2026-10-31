# Versions and deprecation

trmtr-api has a version of its own, separate from the mod's, and it follows semantic versioning. 1.0.0 is the first
release. Every TRMTR jar, in every edition and loader, carries the same API version at any one time, and the
changelog's API section names it.

Two records hold each version to these rules: `surface-<version>.txt`, the public surface of each minor version, one
line per member; and `format-1.json`, every key of the data format.

## Published records

A record, once its version is published, never changes. `PUBLISHED` lists each published version with the commit it
was published from, and the record must equal its content at that commit; a change to the surface takes the next
version and a record of its own.

## Patch (1.0.x)

Documentation and fixes. Nothing a mod can see changes: the surface and the format's keys are exactly as they were,
and a patch writes no surface record of its own.

## Minor (1.x.0)

Additions only.

- New types, and new methods. An interface a mod implements (`TrmtrPlugin`, `WearListener`, `GroundRule`,
  `WearerRule`, `WearRenderer`) gains methods only as `default` methods, so a plugin compiled against an earlier minor
  keeps loading. An interface marked `@ImplementedByTrmtr` may gain any method, since TRMTR is its only implementer.
- New optional keys and new kinds of data file. An older TRMTR warns once about a key it does not know and goes on,
  or skips the file when its `requires` names the newer minor.
- Something replaced is marked deprecated, never removed: `@Deprecated` in Java, with its replacement named in its
  documentation, and in the data format a key that is still read. It keeps working through every later minor of the
  same major, and its use is logged once per session (once per file, for a data key), naming what replaces it.
- A minor writes a new surface record, `surface-1.x.0.txt`.

## Major (2.0.0)

Removals and incompatible changes, and only of what a published minor had already deprecated. The data `format`
integer moves only here, with a new record, `format-2.json`. A major is announced in the changelog with every
removal and its replacement.

## What a mod should do

- Compile against the oldest API version that has what the mod needs, and name it in `plugin.json`'s `requires`.
- Ask `TrmtrApi.get().atLeast(major, minor)` before calling anything newer than that.
- Never implement an interface marked `@ImplementedByTrmtr`, and never shade the API into the mod's jar.
- When the log says something the mod uses is deprecated, move to the replacement it names before the next major.
