package com.example.trmtr.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;

/** The client's half: mossy loam's color in the world. The models and the color in the hand are the resources' own. */
public final class ExampleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockTintSource moss = state -> 0xFF000000 | ExampleMod.MOSS;
        BlockColorRegistry.register(java.util.List.of(moss), ExampleMod.mossyLoam);
    }
}
