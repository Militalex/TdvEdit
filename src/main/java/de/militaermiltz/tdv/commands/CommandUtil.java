package de.militaermiltz.tdv.commands;

import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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

    // [Debug]
    //public static final List<String> COMMANDS = null;

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
    public static HomogenTuple<Location> transformLocation(Location from, Location to){
        final Location newFrom = new Location(from.getWorld(), Math.min(from.getBlockX(), to.getBlockX()), Math.min(from.getBlockY(), to.getBlockY()), Math.min(from.getBlockZ(), to.getBlockZ()));
        final Location newToo = new Location(from.getWorld(), Math.max(from.getBlockX(), to.getBlockX()), Math.max(from.getBlockY(), to.getBlockY()), Math.max(from.getBlockZ(), to.getBlockZ()));
        return new HomogenTuple<>(newFrom, newToo);
    }

    /**
     * @return Returns world from sender.
     */
    public static World getWorldFromSender(CommandSender sender){
        if (sender instanceof ConsoleCommandSender) throw new IllegalArgumentException("Cannot get World from Console.");

        if (sender instanceof BlockCommandSender) return ((BlockCommandSender) sender).getBlock().getWorld();
        return ((Player)sender).getWorld();
    }

    /**
     * Changes command in command Block.
     */
    public static void setCMDinBlock(Block commandBlock, String newCommand){
        if (!(commandBlock.getState() instanceof CommandBlock)) throw new IllegalArgumentException(commandBlock + " is not an Command Block");

        NBTTileEntity nbtTileEntity = new NBTTileEntity(commandBlock.getState());
        nbtTileEntity.setString("Command", newCommand);
    }

    public static String getCommand(@NotNull String label, @NotNull String[] args){
        final StringBuilder argString = new StringBuilder("/" + label);
        for (String arg : args){
            argString.append(" ").append(arg);
        }

        return argString.toString();
    }

    @Nullable
    public static Boolean changeVolume(CommandSender sender, CommandBlock block, double newValue) {
        //Get Command String
        final String cmd = block.getCommand();

        //Split Command into arguments
        List<String> cmdArgs = Arrays.asList(cmd.split(" "));

        if (cmdArgs.get(0).equals("")){
            return null;
        }

        //Make compatible with execute
        StringBuilder executePrepend = new StringBuilder();
        if (cmdArgs.get(0).equals("execute") || cmdArgs.get(0).equals("/execute")) {

            if (!cmdArgs.contains("run")) {
                sender.sendMessage(ChatColor.RED + "Execute command is not complete. Missing \"run\" argument.");
                return false;
            }

            cmdArgs = new ArrayList<>(cmdArgs);

            //Removes execute command from cmdArgs and store it into executePrepend
            for (int i = 0; i < cmdArgs.size(); ) {
                //Append until "run"
                if (cmdArgs.get(i).equals("run")) {
                    executePrepend.append(cmdArgs.get(i)).append(" ");
                    cmdArgs.remove(i);
                    break;
                }
                executePrepend.append(cmdArgs.get(i)).append(" ");
                cmdArgs.remove(i);
            }
        }

        //Removes temporarily / (only from playsound commands) and saves that / was removed
        boolean haveSlash = false;
        if (cmdArgs.get(0).charAt(0) == '/') {
            cmdArgs.set(0, cmdArgs.get(0).replaceFirst("/", ""));
            haveSlash = true;
        }

        if (cmdArgs.get(0).equals("playsound") && cmdArgs.size() > 7) {
            //Modify the command
            cmdArgs.set(7, "" + newValue);

            StringBuilder builder = new StringBuilder();
            //Prepend execute command
            builder.append(executePrepend);
            //Puts "/" back to the front
            if (haveSlash) builder.append("/");

            //Reconstructs the playsound command
            for (int i = 0; i < cmdArgs.size(); i++) {
                builder.append(cmdArgs.get(i));
                if (i != cmdArgs.size() - 1) builder.append(" ");
            }

            //Sets new command to command Block.
            CommandUtil.setCMDinBlock(block.getBlock(), builder.toString());
            return true;
        }
        return null;
    }
}
