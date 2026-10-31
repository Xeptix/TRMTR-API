package com.example.trmtr;

/**
 * What only a loader's own class can answer for the example's plugin: the game's objects are typed {@code Object} in
 * trmtr-api, and only the edition that made them can look inside.
 *
 * <p>
 * This class names nothing of trmtr-api on purpose. A loader's class sets the edition as soon as the mod is made,
 * before TRMTR loads any plugin, and it may do so whether or not TRMTR is installed: without TRMTR the API's classes
 * are not there, and a class that names none of them still loads.
 */
public final class Example {

    private Example() {}

    /** The example's namespace, and its mod id in every edition. */
    public static final String NAMESPACE = "example";

    /** One edition's answers, given by its loader's class. */
    public interface Edition {

        /** The block at a position in a world, as {@code ns:name}, or null where there is none to name. */
        String blockAt(Object level, int x, int y, int z);

        /** Whether an entity holds the example's charm in its hand. */
        boolean holdsCharm(Object entity);

        /** Whether an entity is a player, whose step a wearer rule never weighs. */
        boolean isPlayer(Object entity);

        /** One line to the example's own log. */
        void say(String line);
    }

    private static volatile Edition edition;

    /** Set once by the loader's class, when the mod is made. */
    public static void use(Edition its) {
        edition = its;
    }

    /** The edition's answers, or null before the loader's class has given them. */
    public static Edition edition() {
        return edition;
    }

    /** Whether a block named in the edition's spelling is one of the example's own. */
    public static boolean ours(String block) {
        return block != null && block.startsWith(NAMESPACE + ":");
    }
}
