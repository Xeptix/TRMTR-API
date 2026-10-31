package com.trmtgtnh.api;

/**
 * Server thread. False keeps the ground there from wearing. Asked before wear lands; any rule saying false keeps it
 * off.
 */
public interface GroundRule {

    boolean mayWear(Object level, int x, int y, int z, String block, Object entity);
}
