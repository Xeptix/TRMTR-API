package com.example.trmtr.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.trmtr.Example;
import com.example.trmtr.ExamplePlugin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * The example mod's main class for 1.21.1 Fabric: it registers the four blocks and the charm, answers what the plugin
 * asks of this edition ({@link Example.Edition}, handed over by {@link ExamplePreLaunch}), and adds
 * {@code /examplewear}. Everything TRMTR is told about the blocks is in the portable data files, and everything the mod
 * does with TRMTR is in the portable {@link ExamplePlugin}; nothing here names TRMTR's own classes.
 *
 * <p>
 * The textures are the game's own, so the example carries no art of its own: mossy loam is grass's top tinted moss
 * green ({@link ExampleClient}), loam is dirt, the step is a 12-pixel slab of dirt, and the runestone is chiselled stone
 * bricks. The blockstate and model files beside the language file name only vanilla textures.
 */
public final class ExampleMod implements ModInitializer {

    /** TRMTR's mod id on 1.21.1. Asked only to decide whether the command, which asks TRMTR, is added. */
    static final String TRMTR = "trmtgtnh";

    /** Mossy loam's color, the same as 1.7.10's. */
    static final int MOSS = 0x6A9A3A;

    static final Logger LOG = LogManager.getLogger("TRMTR API example");

    public static Block mossyLoam;
    public static Block loam;
    public static Block loamStep;
    public static Block runestone;
    public static Item charm;

    @Override
    public void onInitialize() {
        mossyLoam = block(
            "mossy_loam",
            new Block(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .strength(0.6f)
                    .sound(SoundType.GRASS)));
        loam = block(
            "loam",
            new Block(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)));
        loamStep = block(
            "loam_step",
            new Step(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(0.5f)
                    .sound(SoundType.GRAVEL)));
        runestone = block(
            "runestone",
            new Block(
                BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 10f)
                    .sound(SoundType.STONE)));
        charm = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Example.NAMESPACE, "charm"),
            new Item(new Item.Properties().stacksTo(1)));
        // Creative tabs are told their items from 1.19.3: the blocks with the building blocks, the charm with tools.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
            .register(tab -> {
                tab.accept(mossyLoam);
                tab.accept(loam);
                tab.accept(loamStep);
                tab.accept(runestone);
            });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
            .register(tab -> tab.accept(charm));
        // Without TRMTR the plugin is never loaded and the API's classes are not there, so neither is the command.
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> {
            if (FabricLoader.getInstance()
                .isModLoaded(TRMTR)) ExampleWearCommand.register(dispatcher);
        });
    }

    /** One block and its item, under the example's namespace. */
    private static Block block(String name, Block block) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Example.NAMESPACE, name);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties()));
        return block;
    }

    /** The block at a position in a world, as {@code ns:name}. */
    static String blockAt(Object level, int x, int y, int z) {
        if (!(level instanceof Level)) return null;
        Block block = ((Level) level).getBlockState(new BlockPos(x, y, z))
            .getBlock();
        ResourceLocation name = BuiltInRegistries.BLOCK.getKey(block);
        return name == null ? null : name.toString();
    }

    /** Whether an entity holds the charm in its main hand, the one 1.7.10's only hand became. */
    static boolean holdsCharm(Object entity) {
        if (!(entity instanceof LivingEntity) || charm == null) return false;
        ItemStack held = ((LivingEntity) entity).getMainHandItem();
        return !held.isEmpty() && held.getItem() == charm;
    }

    static boolean isPlayer(Object entity) {
        return entity instanceof Player;
    }

    /** The loam step: 12 pixels tall, the shape its data gives TRMTR. */
    static final class Step extends Block {

        static final VoxelShape BOX = Block.box(0, 0, 0, 16, 12, 16);

        Step(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return BOX;
        }

        /** Lit as a slab is: by its shape, so the full face below it stays dark and its open top lets light in. */
        @Override
        public boolean useShapeForLightOcclusion(BlockState state) {
            return true;
        }
    }

    /**
     * {@code /examplewear}: the wear under the player, or at {@code x y z}, through trmtr-api's queries; and
     * {@code /examplewear hook on|off}, which turns the runestone's render hook on or off in this game (in single
     * player the game that draws), so TRMTR's own drawing of the stage pictures can be seen in its place.
     */
    static final class ExampleWearCommand {

        private ExampleWearCommand() {}

        static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(
                Commands.literal("examplewear")
                    .executes(ExampleWearCommand::underFoot)
                    .then(
                        Commands.literal("hook")
                            .then(Commands.literal("on")
                                .executes(context -> hook(context, true)))
                            .then(Commands.literal("off")
                                .executes(context -> hook(context, false))))
                    .then(
                        Commands.argument("x", IntegerArgumentType.integer())
                            .then(
                                Commands.argument("y", IntegerArgumentType.integer())
                                    .then(
                                        Commands.argument("z", IntegerArgumentType.integer())
                                            .executes(ExampleWearCommand::at)))));
        }

        private static int hook(CommandContext<CommandSourceStack> context, boolean on) {
            ExamplePlugin.hook(on);
            tell(context.getSource(), "render hook " + (on ? "on" : "off"));
            return 1;
        }

        private static int at(CommandContext<CommandSourceStack> context) {
            int x = IntegerArgumentType.getInteger(context, "x");
            int y = IntegerArgumentType.getInteger(context, "y");
            int z = IntegerArgumentType.getInteger(context, "z");
            tell(context.getSource(), ExamplePlugin.describe(context.getSource().getLevel(), x, y, z));
            return 1;
        }

        private static int underFoot(CommandContext<CommandSourceStack> context) {
            CommandSourceStack source = context.getSource();
            Entity entity = source.getEntity();
            if (entity == null) {
                tell(source, "/examplewear [<x> <y> <z>] | /examplewear hook <on|off>");
                return 0;
            }
            // The block the entity stands on or in: a sunk or 12-pixel surface puts its feet inside it.
            int x = Mth.floor(entity.getX());
            int y = Mth.floor(entity.getBoundingBox().minY - 0.01);
            int z = Mth.floor(entity.getZ());
            tell(source, ExamplePlugin.describe(source.getLevel(), x, y, z));
            return 1;
        }

        private static void tell(CommandSourceStack source, String line) {
            LOG.info(line);
            source.sendSuccess(() -> Component.literal(line), false);
        }
    }
}
