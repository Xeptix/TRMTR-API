package com.example.trmtr.fabric;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import com.example.trmtr.Example;

/**
 * Gives the plugin this edition's answers ({@link Example.Edition}) before any mod's main entry point runs, and so
 * before TRMTR loads plugins whatever order Fabric Loader runs the main entry points in. The answers name nothing of
 * the game here: each is passed on to {@link ExampleMod}, which is loaded only when one is first asked, long after the
 * game's classes are ready to be loaded.
 */
public final class ExamplePreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Example.use(new Answers());
    }

    /** What the plugin asks of 1.21.1 Fabric, answered by {@link ExampleMod}. */
    static final class Answers implements Example.Edition {

        @Override
        public String blockAt(Object level, int x, int y, int z) {
            return ExampleMod.blockAt(level, x, y, z);
        }

        @Override
        public boolean holdsCharm(Object entity) {
            return ExampleMod.holdsCharm(entity);
        }

        @Override
        public boolean isPlayer(Object entity) {
            return ExampleMod.isPlayer(entity);
        }

        @Override
        public void say(String line) {
            ExampleMod.LOG.info(line);
        }
    }
}
