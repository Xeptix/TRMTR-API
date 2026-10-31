package com.example.trmtr;

import com.trmtgtnh.api.Queries;
import com.trmtgtnh.api.Registrar;
import com.trmtgtnh.api.Trmtr;
import com.trmtgtnh.api.TrmtrApi;
import com.trmtgtnh.api.TrmtrPlugin;
import com.trmtgtnh.api.Wear;
import com.trmtgtnh.api.WearContext;
import com.trmtgtnh.api.WearDraw;
import com.trmtgtnh.api.WearListener;
import com.trmtgtnh.api.WearRenderer;
import com.trmtgtnh.api.WearerRule;

/**
 * The example mod's plugin, named in {@code assets/example/trmtr/plugin.json}: one copy for every edition and loader,
 * using trmtr-api and nothing else, as any mod would.
 *
 * <ul>
 * <li>A wearer rule: a mob holding the example's charm wears the ground half as hard.</li>
 * <li>A listener: a player holding the charm wears it half as hard too (a wearer rule weighs mobs, never a player's
 * step), and every change of wear on one of the example's blocks is said in the example's log.</li>
 * <li>A render hook for {@code example:runestone}: a rune inlaid in the worn top, deeper with each stage.</li>
 * <li>A query, {@link #describe}, which each loader's {@code /examplewear} command answers with.</li>
 * </ul>
 *
 * The data the example gives (its families, looks and shapes) is in its files under {@code assets/example/trmtr/},
 * not here: most of what a mod tells TRMTR needs no code.
 */
public final class ExamplePlugin implements TrmtrPlugin {

    /** The block the render hook draws. */
    public static final String RUNESTONE = Example.NAMESPACE + ":runestone";

    /** How much the charm leaves of the wear its holder would make. */
    static final float CHARMED = 0.5f;

    /**
     * Whether the render hook draws. Off, it hands the runestone back to TRMTR, which then draws the stage pictures a
     * pack gives it, so both can be seen in one world.
     */
    private static volatile boolean hookOn = true;

    /** A public constructor without parameters, which is how TRMTR makes a plugin. */
    public ExamplePlugin() {}

    @Override
    public void register(Registrar registrar) {
        registrar.wearerRule(new CharmedMobs());
        registrar.wearListener(new Listener());
        registrar.renderer(RUNESTONE, new Runes());
        Example.Edition edition = Example.edition();
        Trmtr trmtr = TrmtrApi.get();
        if (edition != null && trmtr != null) {
            edition.say(
                "plugin registered with TRMTR (trmtr-api " + trmtr.version()
                    + ", data format "
                    + trmtr.format()
                    + ", "
                    + trmtr.families()
                        .size()
                    + " families)");
        }
    }

    /** Turns the render hook on or off; what a loader's {@code /examplewear hook} calls. */
    public static void hook(boolean on) {
        hookOn = on;
    }

    /** Whether the render hook draws. */
    public static boolean hook() {
        return hookOn;
    }

    /**
     * The wear at a position as one line: {@code wearAt <x> <y> <z> family=<family> stage=<n>}, or {@code unworn}.
     * Asked on that world's own thread, as trmtr-api answers a position there only.
     */
    public static String describe(Object level, int x, int y, int z) {
        Trmtr trmtr = TrmtrApi.get();
        String at = "wearAt " + x + " " + y + " " + z;
        if (trmtr == null) return at + " - TRMTR is not installed";
        Queries queries = trmtr.queries();
        Wear wear = queries.wearAt(level, x, y, z);
        if (wear == null) {
            String family = queries.familyAt(level, x, y, z);
            return at + " family=" + (family == null ? "none" : family) + " unworn";
        }
        return at + " family="
            + wear.family()
            + " stage="
            + wear.stage()
            + " step="
            + wear.step()
            + "/"
            + wear.steps()
            + (wear.frozen() ? " frozen" : "");
    }

    /** A mob holding the charm wears the ground half as hard. */
    static final class CharmedMobs implements WearerRule {

        @Override
        public float multiplier(Object entity, String entityId, float current) {
            Example.Edition edition = Example.edition();
            return edition != null && edition.holdsCharm(entity) ? current * CHARMED : current;
        }
    }

    /** The player's half of the charm, and a line for every change on the example's blocks. */
    static final class Listener implements WearListener {

        @Override
        public float beforeWear(WearContext context, float amount) {
            Example.Edition edition = Example.edition();
            Object entity = context.entity();
            if (edition == null || entity == null) return amount;
            return edition.isPlayer(entity) && edition.holdsCharm(entity) ? amount * CHARMED : amount;
        }

        @Override
        public void afterChange(Object level, int x, int y, int z, Wear before, Wear after) {
            Example.Edition edition = Example.edition();
            if (edition == null) return;
            String block = edition.blockAt(level, x, y, z);
            if (!Example.ours(block)) return;
            edition.say(
                "afterChange " + x + " " + y + " " + z + " " + block + " stage " + stage(before) + " -> " + stage(after));
        }

        private static String stage(Wear wear) {
            return wear == null ? "unworn" : String.valueOf(wear.stage());
        }
    }

    /**
     * The runestone's rune: three strokes cut into the top, a pixel deeper with each stage up to four. Positions are
     * the block's own, 0 to 1 on each axis.
     */
    static final class Runes implements WearRenderer {

        /** The strokes, each {x0, z0, x1, z1} in pixels across the top. */
        static final int[][] STROKES = { { 7, 3, 9, 13 }, { 4, 5, 7, 7 }, { 9, 9, 12, 11 } };

        @Override
        public boolean draw(WearDraw draw) {
            if (!hookOn) return false;
            Wear wear = draw.wear();
            if (wear == null || wear.stage() <= 0) return false;
            int stage = wear.stage();
            float depth = Math.min(stage, 4) / 16f;
            for (int[] stroke : STROKES) {
                draw.box(stroke[0] / 16f, 1f - depth, stroke[1] / 16f, stroke[2] / 16f, 1f, stroke[3] / 16f, stage);
            }
            return true;
        }
    }
}
