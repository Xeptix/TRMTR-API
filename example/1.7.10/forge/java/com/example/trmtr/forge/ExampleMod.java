package com.example.trmtr.forge;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.trmtr.Example;
import com.example.trmtr.ExamplePlugin;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * The example mod's one class for 1.7.10 Forge: it registers the four blocks and the charm, gives the plugin the
 * answers only this edition has ({@link Example.Edition}), and adds {@code /examplewear}. Everything TRMTR is told
 * about the blocks is in the portable data files, and everything the mod does with TRMTR is in the portable
 * {@link ExamplePlugin}; nothing here names TRMTR's own classes.
 *
 * <p>
 * The textures are the game's own, so the example carries no art of its own: mossy loam is grass's top tinted moss
 * green, loam is dirt, the step is a 12-pixel slab of dirt, and the runestone is chiselled stone brick.
 */
@Mod(modid = Example.NAMESPACE, name = "TRMTR API example", version = "1.0.0", acceptableRemoteVersions = "*")
public final class ExampleMod {

    /** TRMTR's mod id on 1.7.10. Asked only to decide whether the command, which asks TRMTR, is added. */
    static final String TRMTR = "trmtgtnh";

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
    public void preInit(FMLPreInitializationEvent event) {
        mossyLoam = register(
            new Tinted(Material.grass, 0x6A9A3A).setStepSound(Block.soundTypeGrass)
                .setHardness(0.6f)
                .setBlockTextureName("minecraft:grass_top"),
            "mossy_loam");
        loam = register(
            new Plain(Material.ground).setStepSound(Block.soundTypeGravel)
                .setHardness(0.5f)
                .setBlockTextureName("minecraft:dirt"),
            "loam");
        loamStep = register(
            new Step().setStepSound(Block.soundTypeGravel)
                .setHardness(0.5f)
                .setBlockTextureName("minecraft:dirt"),
            "loam_step");
        runestone = register(
            new Plain(Material.rock).setStepSound(Block.soundTypeStone)
                .setHardness(1.5f)
                .setResistance(10f)
                .setBlockTextureName("minecraft:stonebrick_carved"),
            "runestone");
        charm = new Item().setUnlocalizedName(Example.NAMESPACE + ".charm")
            .setTextureName("minecraft:emerald")
            .setMaxStackSize(1)
            .setCreativeTab(CreativeTabs.tabMisc);
        GameRegistry.registerItem(charm, "charm");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Without TRMTR the plugin is never loaded and the API's classes are not there, so neither is the command.
        if (Loader.isModLoaded(TRMTR)) event.registerServerCommand(new ExampleWearCommand());
    }

    private static Block register(Block block, String name) {
        block.setBlockName(Example.NAMESPACE + "." + name)
            .setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(block, name);
        return block;
    }

    /** What the plugin asks of 1.7.10. */
    static final class Answers implements Example.Edition {

        @Override
        public String blockAt(Object level, int x, int y, int z) {
            if (!(level instanceof World)) return null;
            Block block = ((World) level).getBlock(x, y, z);
            Object name = Block.blockRegistry.getNameForObject(block);
            return name == null ? null : name.toString();
        }

        @Override
        public boolean holdsCharm(Object entity) {
            if (!(entity instanceof EntityLivingBase) || charm == null) return false;
            ItemStack held = ((EntityLivingBase) entity).getHeldItem();
            return held != null && held.getItem() == charm;
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

    /** A plain full block; the constructor of {@code Block} is not public on 1.7.10. */
    static class Plain extends Block {

        Plain(Material material) {
            super(material);
        }
    }

    /** A full block drawn in one color over its texture. */
    static final class Tinted extends Plain {

        private final int color;

        Tinted(Material material, int color) {
            super(material);
            this.color = color;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getBlockColor() {
            return color;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int getRenderColor(int meta) {
            return color;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
            return color;
        }
    }

    /** The loam step: 12 pixels tall, the shape its data gives TRMTR. */
    static final class Step extends Plain {

        Step() {
            super(Material.ground);
            setBlockBounds(0f, 0f, 0f, 1f, 0.75f, 1f);
            setLightOpacity(255);
            useNeighborBrightness = true;
        }

        @Override
        public boolean isOpaqueCube() {
            return false;
        }

        @Override
        public boolean renderAsNormalBlock() {
            return false;
        }
    }

    /**
     * {@code /examplewear}: the wear under the player, or at {@code x y z}, through trmtr-api's queries; and
     * {@code /examplewear hook on|off}, which turns the runestone's render hook on or off in this game (in single
     * player the game that draws), so TRMTR's own drawing of the stage pictures can be seen in its place.
     */
    static final class ExampleWearCommand extends CommandBase {

        @Override
        public String getCommandName() {
            return "examplewear";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/examplewear [<x> <y> <z>] | /examplewear hook <on|off>";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 2 && "hook".equals(args[0])) {
                boolean on = "on".equals(args[1]);
                ExamplePlugin.hook(on);
                tell(sender, "render hook " + (on ? "on" : "off"));
                return;
            }
            World world = sender.getEntityWorld();
            int x, y, z;
            if (args.length == 3) {
                x = parseInt(sender, args[0]);
                y = parseInt(sender, args[1]);
                z = parseInt(sender, args[2]);
            } else if (sender instanceof Entity) {
                Entity entity = (Entity) sender;
                // The block the entity stands on or in: a sunk or 12-pixel surface puts its feet inside it.
                x = MathHelper.floor_double(entity.posX);
                y = MathHelper.floor_double(entity.boundingBox.minY - 0.01);
                z = MathHelper.floor_double(entity.posZ);
            } else {
                tell(sender, getCommandUsage(sender));
                return;
            }
            tell(sender, ExamplePlugin.describe(world, x, y, z));
        }

        private static void tell(ICommandSender sender, String line) {
            LOG.info(line);
            sender.addChatMessage(new ChatComponentText(line));
        }
    }
}
