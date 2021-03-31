package de.militaermiltz.tdv.commands;

import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This class includes some Utils used by commands.
 *
 */
public class CommandUtil {

    /**
     * List of all available commands
     */
    public static final List<String> COMMANDS = Bukkit.getServer().getHelpMap().getHelpTopics()
            .stream()
            .map(HelpTopic::getName)
            .collect(Collectors.toList());

    /**
     * @return Returns the coordinates of the Block the Player is looking on.
     */
    public static Location getPlayerLookBlockPos(Player player){
        return player.getTargetBlock(Arrays.stream(Material.values())
                                             .filter(material -> !material.isBlock() || material.isAir() || !material.isSolid())
                                             .collect(Collectors.toSet()), 10).getLocation();
    }

    /**
     * Align from- and to Location to xyz coordinates
     */
    public static Location[] transformLocation(Location from, Location to){
        final Location newFrom = new Location(from.getWorld(), Math.min(from.getBlockX(), to.getBlockX()), Math.min(from.getBlockY(), to.getBlockY()), Math.min(from.getBlockZ(), to.getBlockZ()));
        final Location newToo = new Location(from.getWorld(), Math.max(from.getBlockX(), to.getBlockX()), Math.max(from.getBlockY(), to.getBlockY()), Math.max(from.getBlockZ(), to.getBlockZ()));
        return new Location[]{newFrom, newToo};
    }

    /**
     * @return Returns world from sender.
     */
    public static World getWorldFromSender(CommandSender sender, String[] args){
        if (sender instanceof ConsoleCommandSender) throw new IllegalArgumentException("Cannot get World from Console.");

        if (sender instanceof BlockCommandSender) return ((BlockCommandSender) sender).getBlock().getWorld();
        return ((Player)sender).getWorld();
    }

    /**
     * Changes command in command Block.
     */
    public static void setCommandInCommandBlock(Block commandBlock, String newCommand){
        if (!(commandBlock.getState() instanceof CommandBlock)) throw new IllegalArgumentException(commandBlock + " is not an Command Block");

        NBTTileEntity nbtTileEntity = new NBTTileEntity(commandBlock.getState());
        nbtTileEntity.setString("Command", newCommand);
    }
}
