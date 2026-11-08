package com.example.trmtr.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

/** The client's half: mossy loam's color in the world and in the hand. The models are the resources' own. */
public final class ExampleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockColor moss = (state, world, pos, tint) -> ExampleMod.MOSS;
        ColorProviderRegistry.BLOCK.register(moss, ExampleMod.mossyLoam);
        ItemColor mossInHand = (stack, tint) -> ExampleMod.MOSS;
        ColorProviderRegistry.ITEM.register(mossInHand, ExampleMod.mossyLoam);
    }
}
