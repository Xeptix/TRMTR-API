package com.example.trmtr.forge;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.trmtr.Example;
import com.example.trmtr.ExamplePlugin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

/**
 * The example mod's one class for 1.16.5 Forge: it registers the four blocks and the charm, gives the plugin the
 * answers only this edition has ({@link Example.Edition}), and adds {@code /examplewear}. Everything TRMTR is told
 * about the blocks is in the portable data files, and everything the mod does with TRMTR is in the portable
 * {@link ExamplePlugin}; nothing here names TRMTR's own classes.
 *
 * <p>
 * The textures are the game's own, so the example carries no art of its own: mossy loam is grass's top tinted moss
 * green, loam is dirt, the step is a 12-pixel slab of dirt, and the runestone is chiselled stone bricks. 1.16.5 draws a
 * block from its blockstate and model files, which this loader's resources hold beside the language file; they name
 * only vanilla textures.
 */
@Mod(Example.NAMESPACE)
public final class ExampleMod {

    /** TRMTR's mod id on 1.16.5. Asked only to decide whether the command, which asks TRMTR, is added. */
    static final String TRMTR = "trmtgtnh";

    /** Mossy loam's color, the same as 1.7.10's. */
    static final int MOSS = 0x6A9A3A;

    static final Logger LOG = LogManager.getLogger("TRMTR API example");

    public static Block mossyLoam;
    public static Block loam;
    public static Block loamStep;
    public static Block runestone;
    public static Item charm;

    public ExampleMod() {
        // Set when the mod is made, before any setup event and so before TRMTR loads plugins (its mods.toml also
        // asks to be made before TRMTR). Example names nothing of trmtr-api, so this is safe without TRMTR installed.
        Example.use(new Answers());
    }

    /** 1.16.5 registers blocks and items in Forge's registry events, on the mod's own bus. */
    @Mod.EventBusSubscriber(modid = Example.NAMESPACE, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {

        private Registration() {}

        @SubscribeEvent
        public static void blocks(RegistryEvent.Register<Block> event) {
            mossyLoam = named(
                new Block(
                    BlockBehaviour.Properties.of(Material.GRASS)
                        .strength(0.6f)
                        .sound(SoundType.GRASS)),
                "mossy_loam");
            loam = named(
                new Block(
                    BlockBehaviour.Properties.of(Material.DIRT)
                        .strength(0.5f)
                        .sound(SoundType.GRAVEL)),
                "loam");
            loamStep = named(
                new Step(
                    BlockBehaviour.Properties.of(Material.DIRT)
                        .strength(0.5f)
                        .sound(SoundType.GRAVEL)),
                "loam_step");
            runestone = named(
                new Block(
                    BlockBehaviour.Properties.of(Material.STONE)
                        .strength(1.5f, 10f)
                        .sound(SoundType.STONE)),
                "runestone");
            event.getRegistry()
                .registerAll(mossyLoam, loam, loamStep, runestone);
        }

        @SubscribeEvent
        public static void items(RegistryEvent.Register<Item> event) {
            for (Block block : ExampleMod.blocks()) {
                event.getRegistry()
                    .register(
                        new BlockItem(block, new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS))
                            .setRegistryName(block.getRegistryName()));
            }
            charm = new Item(
                new Item.Properties().tab(CreativeModeTab.TAB_MISC)
                    .stacksTo(1)).setRegistryName(Example.NAMESPACE, "charm");
            event.getRegistry()
                .register(charm);
        }

        private static Block named(Block block, String name) {
            return block.setRegistryName(Example.NAMESPACE, name);
        }
    }

    static Block[] blocks() {
        return new Block[] { mossyLoam, loam, loamStep, runestone };
    }

    /** The command, on Forge's own bus; without TRMTR the plugin is never loaded and the API's classes are not there. */
    @Mod.EventBusSubscriber(modid = Example.NAMESPACE)
    public static final class Commanding {

        private Commanding() {}

        @SubscribeEvent
        public static void commands(RegisterCommandsEvent event) {
            if (ModList.get()
                .isLoaded(TRMTR)) ExampleWearCommand.register(event.getDispatcher());
        }
    }

    /** The client's half: mossy loam's color in the world and in the hand. The models are the resources' own. */
    @Mod.EventBusSubscriber(modid = Example.NAMESPACE, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Drawing {

        private Drawing() {}

        @SubscribeEvent
        public static void blockColors(ColorHandlerEvent.Block event) {
            BlockColor moss = (state, world, pos, tint) -> MOSS;
            event.getBlockColors()
                .register(moss, mossyLoam);
        }

        @SubscribeEvent
        public static void itemColors(ColorHandlerEvent.Item event) {
            ItemColor moss = (stack, tint) -> MOSS;
            event.getItemColors()
                .register(moss, mossyLoam);
        }
    }

    /** What the plugin asks of 1.16.5. */
    static final class Answers implements Example.Edition {

        @Override
        public String blockAt(Object level, int x, int y, int z) {
            if (!(level instanceof Level)) return null;
            Block block = ((Level) level).getBlockState(new BlockPos(x, y, z))
                .getBlock();
            ResourceLocation name = Registry.BLOCK.getKey(block);
            return name == null ? null : name.toString();
        }

        @Override
        public boolean holdsCharm(Object entity) {
            if (!(entity instanceof LivingEntity) || charm == null) return false;
            // The main hand, the one 1.7.10's only hand became.
            ItemStack held = ((LivingEntity) entity).getMainHandItem();
            return !held.isEmpty() && held.getItem() == charm;
        }

        @Override
        public boolean isPlayer(Object entity) {
            return entity instanceof Player;
        }

        @Override
        public void say(String line) {
            LOG.info(line);
        }
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
            source.sendSuccess(new TextComponent(line), false);
        }
    }
}
