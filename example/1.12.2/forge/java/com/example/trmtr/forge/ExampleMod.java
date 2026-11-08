package com.example.trmtr.forge;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.trmtr.Example;
import com.example.trmtr.ExamplePlugin;

/**
 * The example mod's one class for 1.12.2 Forge: it registers the four blocks and the charm, gives the plugin the
 * answers only this edition has ({@link Example.Edition}), and adds {@code /examplewear}. Everything TRMTR is told
 * about the blocks is in the portable data files, and everything the mod does with TRMTR is in the portable
 * {@link ExamplePlugin}; nothing here names TRMTR's own classes.
 *
 * <p>
 * The textures are the game's own, so the example carries no art of its own: mossy loam is grass's top tinted moss
 * green, loam is dirt, the step is a 12-pixel slab of dirt, and the runestone is chiselled stone brick. 1.12.2 draws a
 * block from its blockstate and model files, which this edition's resources hold beside the language file; they name
 * only vanilla textures.
 */
@Mod(modid = Example.NAMESPACE, name = "TRMTR API example", version = "1.0.0", acceptableRemoteVersions = "*")
public final class ExampleMod {

    /** TRMTR's mod id on 1.12.2. Asked only to decide whether the command, which asks TRMTR, is added. */
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
        // Set when the mod is made, before any mod's pre-init and so before TRMTR loads plugins. Example names
        // nothing of trmtr-api, so this is safe without TRMTR installed.
        Example.use(new Answers());
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Without TRMTR the plugin is never loaded and the API's classes are not there, so neither is the command.
        if (Loader.isModLoaded(TRMTR)) event.registerServerCommand(new ExampleWearCommand());
    }

    /** 1.12.2 registers blocks and items in Forge's registry events, not in pre-init as 1.7.10 does. */
    @Mod.EventBusSubscriber(modid = Example.NAMESPACE)
    public static final class Registration {

        private Registration() {}

        @SubscribeEvent
        public static void blocks(RegistryEvent.Register<Block> event) {
            mossyLoam = named(
                new Plain(Material.GRASS, SoundType.PLANT).setHardness(0.6f),
                "mossy_loam");
            loam = named(new Plain(Material.GROUND, SoundType.GROUND).setHardness(0.5f), "loam");
            loamStep = named(new Step().setHardness(0.5f), "loam_step");
            runestone = named(
                new Plain(Material.ROCK, SoundType.STONE).setHardness(1.5f)
                    .setResistance(10f),
                "runestone");
            event.getRegistry()
                .registerAll(mossyLoam, loam, loamStep, runestone);
        }

        @SubscribeEvent
        public static void items(RegistryEvent.Register<Item> event) {
            for (Block block : ExampleMod.blocks()) {
                event.getRegistry()
                    .register(
                        new ItemBlock(block).setRegistryName(block.getRegistryName()));
            }
            charm = new Item().setTranslationKey(Example.NAMESPACE + ".charm")
                .setMaxStackSize(1)
                .setCreativeTab(CreativeTabs.MISC)
                .setRegistryName(Example.NAMESPACE, "charm");
            event.getRegistry()
                .register(charm);
        }

        private static Block named(Block block, String name) {
            return block.setTranslationKey(Example.NAMESPACE + "." + name)
                .setCreativeTab(CreativeTabs.BUILDING_BLOCKS)
                .setRegistryName(Example.NAMESPACE, name);
        }
    }

    static Block[] blocks() {
        return new Block[] { mossyLoam, loam, loamStep, runestone };
    }

    /** The client's half: each item's model, and mossy loam's color in the world and in the hand. */
    @Mod.EventBusSubscriber(modid = Example.NAMESPACE, value = Side.CLIENT)
    public static final class Drawing {

        private Drawing() {}

        @SubscribeEvent
        public static void models(ModelRegistryEvent event) {
            for (Block block : ExampleMod.blocks()) {
                ResourceLocation name = block.getRegistryName();
                ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(block),
                    0,
                    new ModelResourceLocation(name, "inventory"));
            }
            ModelLoader.setCustomModelResourceLocation(
                charm,
                0,
                new ModelResourceLocation(charm.getRegistryName(), "inventory"));
        }

        @SubscribeEvent
        public static void blockColors(ColorHandlerEvent.Block event) {
            event.getBlockColors()
                .registerBlockColorHandler(new IBlockColor() {

                    @Override
                    public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tint) {
                        return MOSS;
                    }
                }, mossyLoam);
        }

        @SubscribeEvent
        public static void itemColors(ColorHandlerEvent.Item event) {
            event.getItemColors()
                .registerItemColorHandler(new IItemColor() {

                    @Override
                    public int colorMultiplier(ItemStack stack, int tint) {
                        return MOSS;
                    }
                }, mossyLoam);
        }
    }

    /** What the plugin asks of 1.12.2. */
    static final class Answers implements Example.Edition {

        @Override
        public String blockAt(Object level, int x, int y, int z) {
            if (!(level instanceof World)) return null;
            Block block = ((World) level).getBlockState(new BlockPos(x, y, z))
                .getBlock();
            ResourceLocation name = block.getRegistryName();
            return name == null ? null : name.toString();
        }

        @Override
        public boolean holdsCharm(Object entity) {
            if (!(entity instanceof EntityLivingBase) || charm == null) return false;
            // The main hand, the one 1.7.10's only hand became.
            ItemStack held = ((EntityLivingBase) entity).getHeldItemMainhand();
            return !held.isEmpty() && held.getItem() == charm;
        }

        @Override
        public boolean isPlayer(Object entity) {
            return entity instanceof EntityPlayer;
        }

        @Override
        public void say(String line) {
            LOG.info(line);
        }
    }

    /** A plain full block with its sound; {@code Block.setSoundType} is protected on 1.12.2. */
    static class Plain extends Block {

        Plain(Material material, SoundType sound) {
            super(material);
            setSoundType(sound);
        }
    }

    /** The loam step: 12 pixels tall, the shape its data gives TRMTR. */
    static final class Step extends Plain {

        static final AxisAlignedBB BOX = new AxisAlignedBB(0, 0, 0, 1, 0.75, 1);

        Step() {
            super(Material.GROUND, SoundType.GROUND);
            setLightOpacity(255);
            useNeighborBrightness = true;
        }

        @Override
        public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
            return BOX;
        }

        @Override
        public boolean isOpaqueCube(IBlockState state) {
            return false;
        }

        @Override
        public boolean isFullCube(IBlockState state) {
            return false;
        }

        @Override
        public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
            return face == EnumFacing.DOWN ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
        }
    }

    /**
     * {@code /examplewear}: the wear under the player, or at {@code x y z}, through trmtr-api's queries; and
     * {@code /examplewear hook on|off}, which turns the runestone's render hook on or off in this game (in single
     * player the game that draws), so TRMTR's own drawing of the stage pictures can be seen in its place.
     */
    static final class ExampleWearCommand extends CommandBase {

        @Override
        public String getName() {
            return "examplewear";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return "/examplewear [<x> <y> <z>] | /examplewear hook <on|off>";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length == 2 && "hook".equals(args[0])) {
                boolean on = "on".equals(args[1]);
                ExamplePlugin.hook(on);
                tell(sender, "render hook " + (on ? "on" : "off"));
                return;
            }
            World world = sender.getEntityWorld();
            int x, y, z;
            if (args.length == 3) {
                x = parseInt(args[0]);
                y = parseInt(args[1]);
                z = parseInt(args[2]);
            } else if (sender instanceof Entity) {
                Entity entity = (Entity) sender;
                // The block the entity stands on or in: a sunk or 12-pixel surface puts its feet inside it.
                x = MathHelper.floor(entity.posX);
                y = MathHelper.floor(entity.getEntityBoundingBox().minY - 0.01);
                z = MathHelper.floor(entity.posZ);
            } else {
                tell(sender, getUsage(sender));
                return;
            }
            tell(sender, ExamplePlugin.describe(world, x, y, z));
        }

        private static void tell(ICommandSender sender, String line) {
            LOG.info(line);
            sender.sendMessage(new TextComponentString(line));
        }
    }
}
