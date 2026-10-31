package com.trmtgtnh.api;

/**
 * Server thread.
 */
public interface WearListener {

    /**
     * Asked before every bit of wear, with what caused it. Return 0 to cancel, or a changed amount.
     */
    default float beforeWear(WearContext context, float amount) {
        return amount;
    }

    /**
     * Told after a record changes for any reason, with the record before and after; either is null where there was
     * none.
     */
    default void afterChange(Object level, int x, int y, int z, Wear before, Wear after) {}
}
