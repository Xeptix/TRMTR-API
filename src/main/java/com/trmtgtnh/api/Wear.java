package com.trmtgtnh.api;

/**
 * One record of worn ground.
 */
@ImplementedByTrmtr
public interface Wear {

    /** What the ground is. */
    String family();

    /** The family it is drawn as by now, which differs once it has worn through. */
    String appearance();

    /** The drawn stage. */
    int stage();

    /** How far it has sunk. */
    int sinkPixels();

    /** Its place on its run. */
    int step();

    /** The run's length. */
    int steps();

    /** Its step as a share of its run. */
    float fraction();

    /** Whether it is held as it is. */
    boolean frozen();

    /** Its reinforcement level, 0 to 3. */
    int reinforcement();

    /** The server's count toward the next step. -1 on a client, which is not told it. */
    float progress();
}
