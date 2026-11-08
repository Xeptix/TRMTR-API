# The TRMTR Java API (trmtr-api 1.0.0)

Most of what a mod wants from TRMT: Reimagined - which of its blocks wear, how they look, what shape they sink into,
which of its mobs wear the ground - is data, and `FORMAT.md` covers it without any Java. The Java API is for the rest:
hearing when ground wears, deciding where it may, weighing a mob by something only the mod knows, reading wear, and
drawing worn ground a mod's own way.

The API is the package `com.trmtgtnh.api`. It is the same in every edition from 1.7.10 to 26.3, it is compiled at the
Java 8 language level, and it names no class of the game: where the game's own object is passed, the type is
`Object`, and the table at the end names the class each edition passes.

## Using it

Depend on it at compile time only:

```
repositories {
    maven { url "https://raw.githubusercontent.com/Xeptix/TRMTR-API/main/maven" }
}
dependencies {
    compileOnly "com.trmtgtnh:trmtr-api:1.0.0"
}
```

or, from Modrinth's Maven (`https://api.modrinth.com/maven`), `compileOnly "maven.modrinth:trmtr-api:1.0.0"`. The jar
names no game class, so plain `compileOnly` works under Loom and ForgeGradle alike.

Then name the plugin class in `assets/<your namespace>/trmtr/plugin.json` (`FORMAT.md`, "plugin.json"). Nothing else
is needed: no registration call, no event, no dependency at run time.

- **Never shade the API into your jar.** TRMTR carries these classes in its own jar in every edition and is their only
  provider. TRMTR checks at start that the API came from its own jar, and if another jar carries a copy it names that
  jar once in the log.
- **Without TRMTR installed, your plugin is never loaded** and no API class is touched, so a mod can support TRMTR
  without requiring it.
- **The plugin is loaded once**, after TRMTR has read the data files and before it first works out which block is in
  which family, by TRMTR's own class loader, which sees every mod.

## The surface

This is the whole public surface of 1.0.0. Bodies are TRMTR's own and left out. `surface-1.0.0.txt` is the same
surface as a list, one line per member, and is what each later version is checked against.

```java
package com.trmtgtnh.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/** Where a mod reaches TRMTR. */
public final class TrmtrApi {
    /** This API's version, which is not the mod's. */
    public static final String VERSION = "1.0.0";
    /** TRMTR itself. Set before any plugin is called. */
    public static Trmtr get() { ... }
}

/** Marks an interface TRMTR implements. A mod calls it and never implements it. */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ImplementedByTrmtr {}

/** A mod's plugin, named in its plugin.json. */
public interface TrmtrPlugin {
    void register(Registrar registrar);
}

/** Handed to a plugin's register, and only valid during that call. */
@ImplementedByTrmtr
public interface Registrar {
    void wearListener(WearListener listener);
    void groundRule(GroundRule rule);
    void wearerRule(WearerRule rule);
    /** For blocks of the plugin's own namespace only. */
    void renderer(String blockSelector, WearRenderer renderer);
    /** A data file from code: name is its path under trmtr/, such as "families/loam.json". */
    void data(String name, String json);
}

@ImplementedByTrmtr
public interface Trmtr {
    String version();
    boolean atLeast(int major, int minor);
    int format();
    List<String> families();
    Queries queries();
    Actions actions();
}

/** By key, any thread; by position, that world's own thread only. Never a half-built table. */
@ImplementedByTrmtr
public interface Queries {
    /** Null when the block does not wear. Any thread. */
    String familyOf(String blockSelector);
    /** On 1.7.10 a Block, which answers only where its every metadata value is in one family. Any thread. */
    String familyOf(Object blockState);
    /** The block there as it stands, with its metadata or state. Null when it does not wear. That world's thread. */
    String familyAt(Object level, int x, int y, int z);
    /** A family's own chain. Any thread. */
    int chainLength(String family);
    /** The chain of the block there, its own wearsThroughTo counted. 0 when it does not wear. That world's thread. */
    int chainLengthAt(Object level, int x, int y, int z);
    /** Null when the ground there is unworn. A client's world or a server's, on that world's thread. */
    Wear wearAt(Object level, int x, int y, int z);
}

@ImplementedByTrmtr
public interface Wear {
    String family();
    String appearance();
    int stage();
    int stages();
    int sinkPixels();
    int step();
    int steps();
    float fraction();
    boolean frozen();
    int reinforcement();
    /** -1 on a client, which is not told it. */
    float progress();
}

/** Server thread only. Elsewhere each returns false, and says so once. */
@ImplementedByTrmtr
public interface Actions {
    boolean wear(Object level, int x, int y, int z, float amount);
    boolean restore(Object level, int x, int y, int z, int steps);
    boolean freeze(Object level, int x, int y, int z, boolean frozen);
    boolean forget(Object level, int x, int y, int z);
}

/** Server thread. */
public interface WearListener {
    /** Return 0 to cancel, or a changed amount. */
    default float beforeWear(WearContext context, float amount) { ... }
    default void afterChange(Object level, int x, int y, int z, Wear before, Wear after) { ... }
}

@ImplementedByTrmtr
public interface WearContext {
    Object level();
    int x();
    int y();
    int z();
    String block();
    /** Null when nothing walked there: a blast, a command or a call through Actions. */
    Object entity();
    Cause cause();

    enum Cause { STEP, MOUNT, BLAST, IMPACT, GOLEM, COMMAND, API }
}

/** Server thread. False keeps the ground there from wearing. */
public interface GroundRule {
    boolean mayWear(Object level, int x, int y, int z, String block, Object entity);
}

/** Server thread. Returns the mob's multiplier of a player's step, given the one TRMTR worked out. */
public interface WearerRule {
    float multiplier(Object entity, String entityId, float current);
}

/** The thread that builds chunks for drawing. False lets TRMTR draw as usual. */
public interface WearRenderer {
    boolean draw(WearDraw draw);
}

/** Only valid during draw. No world access: everything needed is here. */
@ImplementedByTrmtr
public interface WearDraw {
    int x();
    int y();
    int z();
    String block();
    Wear wear();
    Object sprite(int face);
    int tint(int face);
    int light();
    void quad(float[] xyz, float[] uv, int face, int tint);
    void box(float x0, float y0, float z0, float x1, float y1, float z1, int stage);
    Object nativeContext();
}
```

## What each part does

- **`TrmtrApi.get()`** gives TRMTR itself. `version()` is the API version TRMTR carries, and `atLeast(1, 1)` is how a
  mod built against a later minor asks whether a method it wants is there. `format()` is the newest data format it
  reads. `families()` is every family's key, in TRMTR's order.
- **`Registrar`** is handed to `TrmtrPlugin.register` once. What is registered lasts the session.
  `renderer` takes a block selector of the plugin's own namespace only; one for another namespace's block is refused
  with one line. `data` takes a data file as text, and it counts as the plugin's own mod's jar in the load order.
- **`Queries`** by key, `familyOf` and `chainLength`, can be asked from any thread: they read only tables TRMTR
  publishes whole. Queries by position, `familyAt`, `chainLengthAt` and `wearAt`, read the world and its records as
  they stand, so each is answered only on that world's own thread - the server's for a server's world, the client's
  for a client's (the table at the end names them) - and anywhere else gives its no-answer, null or 0, and TRMTR says
  so once in the log. `familyOf` returns a family's key, or null for a block that does not wear. `familyAt` asks the same of the block at a position, read with its metadata (1.7.10) or its state (1.12.2
  on), and is the one form that answers for any block in every edition. `chainLength` is how many steps a family's
  run takes from untouched to the deepest rut; `chainLengthAt` is the same for the block at a position, on the block's
  own chain once a data file gives that block a `wearsThroughTo` of its own (`FORMAT.md`, "families/"): the same
  length as its family's, except a grass block's, whose list decides the depth its run sinks to.
  `wearAt` reads the record at a position: on the server, the whole record; on a client, what the server has sent
  it, with `progress()` -1.
- **Asking about a block on 1.7.10.** A 1.7.10 `Block` is not the whole block: its metadata, which a family can be
  chosen by, is held in the world beside it, so a `Block` on its own cannot say which of its metadata values is
  meant. `familyOf(Object)` therefore answers for a `Block` only when every one of its metadata values is in the same
  family; where they differ it returns null and says once in the log which form to use instead. A block with its
  metadata is asked about as `familyOf("ns:name:3")`, or where it stands with `familyAt` and `chainLengthAt`. This
  keeps the surface the same in all eight editions, with no game type and no signature of 1.7.10's own, and no
  question ever answers with another metadata value's family.
- **`Wear`** is one record. `family()` is what the ground is; `appearance()` the family it is drawn as by now, which
  differs once it has worn through. `stage()` is the drawn stage and `stages()` how many this stretch of
  the run has, the face's while it stands flat and one pixel's once sunk; `sinkPixels()` is how far the ground is drawn
  sunk there: `wearAt` and `WearDraw.wear()` give the depth drawn on that block by the same rule the drawing uses, so it
  is nought where the world has its depth off or a shape says `"sinks": false`, and it is held by the family's cap, a
  slab's or stair's half of it, and a shape's `maxSinkPixels` and height, though the record goes on counting. The one
  limit: a record `WearListener.afterChange` is handed where the engine holds only its chain and not its block counts
  the depth switch and the family's cap but not the block's shape, so a listener that needs the exact depth asks
  `wearAt`. `step()` and
  `steps()` its place on its run and the run's length (the block's own chain, where it has one), and `fraction()`
  the two as a share. `frozen()` is whether it is held as it is, `reinforcement()` its reinforcement level, 0 to 3, and `progress()` the server's count toward the next
  step.
- **`Actions`** change the ground, on the server thread only: `wear` adds wear as if that much traffic had passed,
  `restore` mends steps, `freeze` holds or releases a record, and `forget` drops a record so the ground is untouched.
  Each returns whether it did anything. Each passes through the listeners and rules with the cause `API`. An amount
  given to `wear` is held to at most 1,000,000.
- **`WearListener`**: `beforeWear` is asked before every bit of wear, with what caused it, and may return a smaller,
  larger or zero amount; a larger one is held to at most 1,000,000, as is a `WearerRule`'s multiplier, so the wear a
  record banks stays a number. `afterChange` is told after a record changes for any reason - wear, healing, a tamper, a
  command - with the record before and after (either is null where there was none): at once, or at the end of the
  tick for a change made while a chunk loaded ("Chunk loading").
- **`WearContext.Cause`**: `STEP` a player or mob walking, `MOUNT` a ridden mob or vehicle, `BLAST` an explosion,
  `IMPACT` something landing hard from a height, `GOLEM` the Golem of Ways, `COMMAND` a TRMTR command, `API` a call through `Actions`.
- **`GroundRule`** is asked before wear lands; any rule saying false keeps it off.
- **`WearerRule`** is asked for a mob's multiplier after TRMTR has worked out its own and before the player's mob list
  is applied, so the player's list still wins.
- **`WearRenderer`** draws worn ground for a block. `WearDraw` gives the position, the block, its record, its own
  sprites and tints, and the light; `quad` and `box` add geometry, with `box` taking a stage so the shape can deepen as
  the ground wears. `nativeContext()` is the edition's own drawing object, for a mod that needs more than `quad` and
  `box`. Returning false hands the block back to TRMTR.

## Chunk loading

A chunk's load event comes on the server thread while the chunk is still being finished. From 1.16.5 on, a read of
that chunk through the world from there waits for a step that is waiting for the reader, and the server stops for good;
on 1.7.10 and 1.12.2 the read answers, but a read of the chunk beside it loads that one inside the first. So nothing of
a plugin's runs inside a chunk's load or unload, in any edition, and a hook may read the world and ask for chunks as any
server code may.

- **`afterChange` waits for the tick.** A chunk coming back is healed for the time it was away as it arrives. What that
  changes is held and told at the end of the server tick, on the server thread, with the chunk whole, in the order it
  happened. A change after it waits behind it, so the listeners hear every change in order. `before` and `after` say
  what the record was then; the record and the world may have moved on, and the chunk may even have unloaded again, in
  which case a read of it loads it as any server read does.
- **How much waits.** At most 4,096 changes are told in a tick and the rest go on the next. At most 65,536 wait; past
  that a change is not told, said once in the log. What still waits when the server stops is dropped, as the world it
  names has gone, so a mod keeping its own copy of what is worn reads it afresh when a world opens.
- **A rule cannot wait.** A `GroundRule`, `beforeWear` and a `WearerRule` give an answer, so they are asked only outside
  chunk loading: a chunk loading wears nothing, so TRMTR has no reason to ask there. Were another way in to reach one
  inside a load, the rule is not called: the wear does not land, a multiplier stays as TRMTR worked it out, and it is
  said once in the log.
- **A mod's own load handler.** `Queries` and `Actions` never load a chunk or wait for one. A position whose chunk is not
  loaded and whole, which from 1.16.5 on includes a chunk in its own load event, gives a query's no-answer and an
  action's false. A mod wanting to act on a chunk as it arrives keeps the position and acts on the next tick.

## Drawing a hooked block

- **Units.** Everything a `WearDraw` takes is in block units, 0 to 1 on each axis from the block's lower corner (its
  smallest x, y and z), whatever the edition draws in. y 1 is the ground's drawn top, which is below the cell's once
  the ground has sunk, so what a hook draws at the top stays at the surface as the ground goes down.
- **Faces** are numbered as the game numbers directions: 0 down, 1 up, 2 north, 3 south, 4 west, 5 east.
- **`box` cuts.** Each box is taken out of the block; boxes that overlap cut their union. A box is clamped to the
  block and its corners may be given either way round. Once `draw` returns true TRMTR draws what is left: the block's
  own faces where they still stand, with the pictures it would draw there, and the floor and walls each cut opens,
  which take the picture TRMTR draws at that box's `stage` (0 or less: the block's own sprite), on the floor and the
  walls alike. Every int is a `stage`, the smallest included. A hook that returns true having cut nothing draws the
  block whole. Up to 16 boxes a draw; past that a box is ignored, said once. A grass side keeps its tinted fringe.
- **`quad` adds** one quad over what is left: four corners, x y z each, counter-clockwise seen from the side it faces;
  uv four pairs, 0 to 1 across the block's own sprite on `face`; tint `0xRRGGBB`, multiplied with that face's light.
  From 26.1 a quad holds no color of its own, so it takes one of sixteen tint slots of the block: the first sixteen
  colors of a draw are told apart, and any past them takes the sixteenth's.
- **Light.** A hooked block is drawn flat, with no smooth lighting. An outer side and the underside are lit by the cell
  each looks into; the top, the floors and walls a cut opens, and every `quad` by the cell above the block, which is what
  `light()` reports. So a cut is lit as the ground beside it is, however deep it goes. A quad put straight into
  `nativeContext()` is the mod's own: on the editions where that is a list of baked quads it goes with no side and the
  game lights it from where it stands, which under a cut is dark. What falls short of this, each still drawn: Embeddium
  on 1.16.5 and a Canvas material on 1.16.5 keep smooth lighting, so a cut's corners draw darker than its middle; OptiFine
  from 1.16.5 is not held to it.
- **Nothing is drawn until `draw` returns true.** What `box` and `quad` were given is kept and drawn after, so a hook
  that returns false or throws leaves nothing behind and TRMTR draws the block as usual. If TRMTR cannot draw what was
  asked for (a picture that will not resolve or a face with none, say), it takes back what it had written, and what
  the hook wrote to `nativeContext()`, and draws the block as usual, said once. On 1.7.10, where another mod has
  changed the tessellator so that TRMTR cannot read where its buffer stands, hooks are not asked at all and TRMTR
  draws every hooked block itself, said once; where it cannot set the buffer back, the block is left as far as it was
  drawn rather than drawn twice, said once. Only what a mod writes straight to `nativeContext()` goes in at once.
- **Which renderer.** The most particular selector that takes the block wins: one naming its metadata or properties
  before one naming the block alone, a tag last; between equals, the first registered.
- **Where it is reached.** 1.7.10 calls the hook from the standard block renderer's own entry point, so vanilla's
  mesher and Angelica's (its Celeritas mesher draws each block through that entry point into the tessellator it
  meshes) both draw it.

## Failures

A plugin is never allowed to stop the game. Loading the plugin's class and every call into a plugin, listener, rule or
renderer is caught. A hook that throws is logged once with its stack, naming the mod, and switched off for the rest
of the session; the rest of the mod's hooks go on.

## How it changes

`DEPRECATION.md` is the policy. In short: a patch changes nothing a mod can see; a minor only adds; a major may remove
what an earlier minor deprecated. Interfaces a mod implements (`TrmtrPlugin`, `WearListener`, `GroundRule`,
`WearerRule`, `WearRenderer`) only ever gain `default` methods, so a plugin compiled against 1.0.0 keeps loading.
Interfaces marked `@ImplementedByTrmtr` may gain methods in a minor, which is why a mod must never implement them.

## The objects each edition passes

Every parameter typed `Object` is the edition's own class, below. From 1.16.5 the names are Mojang's, which are the
names TRMTR itself is written against; a Fabric mod compiled with other mappings sees the same class under its own
mappings' name.

| Edition | Loaders | `level` | `entity` | `familyOf(Object)` takes | `WearDraw.sprite` | `WearDraw.nativeContext` | Selectors | A position is asked on |
|---|---|---|---|---|---|---|---|---|
| 1.7.10 | Forge | `net.minecraft.world.World` | `net.minecraft.entity.Entity` | `net.minecraft.block.Block`, answered only where all its metadata values share a family ("Asking about a block on 1.7.10") | `net.minecraft.util.IIcon` | `net.minecraft.client.renderer.Tessellator` | `ns:name`, `ns:name:3`, `ns:name:*` | the server thread for a server's `World`, the client thread for a client's |
| 1.12.2 | Forge | `net.minecraft.world.World` | `net.minecraft.entity.Entity` | `net.minecraft.block.state.IBlockState` | `net.minecraft.client.renderer.texture.TextureAtlasSprite` | `List<net.minecraft.client.renderer.block.model.BakedQuad>` | as 1.7.10, and `ns:name[prop=value]` | as 1.7.10 |
| 1.16.5 | Forge, Fabric | `net.minecraft.world.level.Level` | `net.minecraft.world.entity.Entity` | `net.minecraft.world.level.block.state.BlockState` | `net.minecraft.client.renderer.texture.TextureAtlasSprite` | `List<net.minecraft.client.renderer.block.model.BakedQuad>` | `ns:name`, `ns:name[prop=value]` on every property, `#ns:tag` | the server thread for a server's `Level`, the client's main thread for a client's |
| 1.20.1 | Forge, Fabric | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 |
| 1.21.1 | Forge, NeoForge, Fabric | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 |
| 26.1.2, 26.2, 26.3 | Forge, NeoForge, Fabric | as 1.16.5 | as 1.16.5 | as 1.16.5 | as 1.16.5 | `List<net.minecraft.client.resources.model.geometry.BakedQuad>` | as 1.16.5 | as 1.16.5 |

Positions are always three ints. Blocks and entities passed as text (`WearContext.block()`, `GroundRule`'s `block`,
`WearerRule`'s `entityId`) are in the edition's own spelling, as `FORMAT.md` describes for selectors.
