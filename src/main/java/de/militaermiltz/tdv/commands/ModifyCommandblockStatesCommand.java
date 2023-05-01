package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.util.HomogenTuple;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTTileEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Militalex
 * @version 1.0
 *
 * This class defines modifycommandblockstates command, which can be used to change all physical aspects of a commandblock e.g.
 * alwaysactive, repeating, chain, ... .
 *
 */
public class ModifyCommandblockStatesCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String commandString = CommandUtil.getCommand(label, args);

        if (sender instanceof ConsoleCommandSender) sender.sendMessage("This command is only usable by players or command blocks.");
        if (!isCorrect(commandString)) return false;

        // Calculate from->to Location
        final World world = CommandUtil.getWorldFromSender(sender);
        final HomogenTuple<Location> fromTo = CommandUtil.transformLocation(new Location(world, Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]), Integer.parseInt(args[2])),
                new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));

        final Location from = fromTo.getKey();
        final Location to = fromTo.getValue();

        final String mode = args[6];
        
        //Count how many cmd-Blocks are modified
        int modified = 0;

        for (int x = from.getBlockX(); x <= to.getBlockX(); x++) {
            for (int y = from.getBlockY(); y <= to.getBlockY(); y++) {
                for (int z = from.getBlockZ(); z <= to.getBlockZ(); z++) {
                    final Block block = world.getBlockAt(x, y, z);

                    if (block.getState() instanceof CommandBlock) {
                        String blockString = block.getState().getBlockData().getAsString();

                        // Copy states of commandblock
                        final NBTTileEntity nbtTileEntity = new NBTTileEntity(block.getState());

                        final Path path = Paths.get("plugins/TdvEdit/tmp.nbt");

                        NBTFile nbtData;
                        try {
                            nbtData = new NBTFile(path.toFile());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }
                        CommandUtil.copyNBTData(nbtTileEntity, nbtData);

                        // modify blockStateString
                        if (mode.equals("conditional")) blockString = blockString.replace("conditional=false", "conditional=true");
                        else if (mode.equals("unconditional")) blockString = blockString.replace("conditional=true", "conditional=false");

                        blockString =
                                ((mode.equals("cb")) ? "minecraft:command_block" :
                                (mode.equals("rcb")) ? "minecraft:repeating_command_block" :
                                (mode.equals("ccb")) ?"minecraft:chain_command_block" :
                                block.getType().getKey())
                                + blockString.substring(blockString.indexOf("["));

                        // Placing the block if was changed
                        if (!blockString.equals(block.getState().getBlockData().getAsString())) {
                            Bukkit.getServer().dispatchCommand(sender, "setblock " + x + " " + y + " " + z + " " + blockString
                            );
                        }

                        // Modified correction
                        else if (!mode.equals("needsRedstone") && !mode.equals("alwaysActive")){
                            modified--;
                        }

                        // modify commandblock nbt data aspects if caller aks for
                        if (mode.equals("needsRedstone")) {
                            if (nbtData.getBoolean("auto")) {
                                nbtData.setBoolean("auto", false);
                            }
                            else modified--;
                        }
                        else if (mode.equals("alwaysActive")) {
                            if (!nbtData.getBoolean("auto")) {
                                nbtData.setBoolean("auto", true);
                            }
                            else modified--;
                        }

                        CommandUtil.copyNBTData(nbtData, new NBTTileEntity(block.getState()));

                        try {
                            nbtData.save();
                            Files.deleteIfExists(path);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            return true;
                        }

                        modified++;
                    }
                }
            }
        }
        if (modified == 0) sender.sendMessage(ChatColor.RED + "No commands to modify.");
        else sender.sendMessage("" + ChatColor.RED + modified + ChatColor.GOLD + " command(s) are modified.");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) return Collections.emptyList();

        final List<String> list = new ArrayList<>();
        final String cmdS = CommandUtil.getCommand(alias, args);

        if (cmdS.matches("/modifycommandblockstates ")
                || cmdS.matches("/modifycommandblockstates (-?(\\d)+ ){3}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockX());
            return list;
        }
        else if (cmdS.matches("/modifycommandblockstates (-?(\\d)+ ){1}")
                || cmdS.matches("/modifycommandblockstates (-?(\\d)+ ){4}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockY());
            return list;
        }
        else if (cmdS.matches("/modifycommandblockstates (-?(\\d)+ ){2}")
                || cmdS.matches("/modifycommandblockstates (-?(\\d)+ ){5}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockZ());
            return list;
        }
        else if (cmdS.matches("/modifycommandblockstates (-?(\\d)+(.\\d+)? ){6}")){
            return Arrays.asList("cb", "rcb", "ccb", "conditional", "unconditional", "needsRedstone", "alwaysActive");
        }
        else return Collections.emptyList();
    }

    private boolean isCorrect(String command){
        return command.matches("/modifycommandblockstates (-?(\\d)+ ){6}(cb|rcb|ccb|conditional|unconditional|needsRedstone|alwaysActive)");
    }
}
