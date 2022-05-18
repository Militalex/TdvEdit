package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.util.Direction;
import de.militaermiltz.tdv.util.HomogenTuple;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CrescendoCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender){
            sender.sendMessage("Sorry, only Players and Commandblocks can execute this command.");
            return true;
        }
        else{
            if (!isCorrect(CommandUtil.getCommand(label, args))){
                return false;
            }
            final double min = Double.parseDouble(args[6]), max = Double.parseDouble(args[7]);

            if (min >= max){
                sender.sendMessage(ChatColor.RED + "Min cannot larger or equal to Max.");
                return false;
            }

            final World world = CommandUtil.getWorldFromSender(sender);
            final Direction dir = Direction.valueOf(args[5].toUpperCase());
            Location startLoc = new Location(world, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            Location endLoc = new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[1]), Integer.parseInt(args[4]));

            final HomogenTuple<Location> alignedLoc = CommandUtil.transformLocation(startLoc, endLoc);
            if (!dir.isPointingNegative()){
                startLoc = alignedLoc.getKey();
                endLoc = alignedLoc.getValue();
            }
            else{
                startLoc = alignedLoc.getValue();
                endLoc = alignedLoc.getKey();
            }

            final double anzCom = calculateAnzCom(world, startLoc, endLoc, dir);

            if (anzCom == 0){
                sender.sendMessage(ChatColor.RED + "No commands to modify.");
                return true;
            }

            final double stepSize = (max - min) / ((anzCom == 1) ? anzCom : anzCom - 1);

            double step = min;
            double x, y = startLoc.getBlockY(), z;
            int modifiedRow = 0;
            int modfied = 0;

            if (dir == Direction.EAST || dir == Direction.WEST){
                for (x = startLoc.getBlockX(); (dir == Direction.WEST) ? x >= endLoc.getBlockX() : x <= endLoc.getBlockX(); x = (dir == Direction.WEST) ? x - 1 : x + 1){
                    boolean flag = false;

                    for (z = startLoc.getBlockZ(); (dir == Direction.WEST) ? z >= endLoc.getBlockZ() : z <= endLoc.getBlockZ(); z = (dir == Direction.WEST) ? z - 1 : z + 1){
                        final Block block = world.getBlockAt((int)x, (int)y, (int)z);
                        if (block.getState() instanceof CommandBlock){

                            Boolean result = CommandUtil.changeVolume(sender, (CommandBlock) block.getState(), step);
                            if (result != null && !result) return true;
                            if (result != null) {
                                flag = true;
                                modfied++;
                            }
                        }
                    }

                    if (flag){
                        step += stepSize;
                        modifiedRow++;
                    }
                }
            }
            else{
                for (z = startLoc.getBlockZ(); (dir == Direction.NORTH) ? z >= endLoc.getBlockZ() : z <= endLoc.getBlockZ(); z = (dir == Direction.NORTH) ? z - 1 : z + 1){
                    boolean flag = false;

                    for (x = startLoc.getBlockX(); (dir == Direction.NORTH) ? x >= endLoc.getBlockX() : x <= endLoc.getBlockX(); x = (dir == Direction.NORTH) ? x - 1 : x + 1){
                        final Block block = world.getBlockAt((int)x, (int)y, (int)z);
                        if (block.getState() instanceof CommandBlock){

                            Boolean result = CommandUtil.changeVolume(sender, (CommandBlock) block.getState(), step);
                            if (result != null && !result) return true;
                            if (result != null) {
                                flag = true;
                                modfied++;
                            }
                        }
                    }

                    if (flag){
                        step += stepSize;
                        modifiedRow++;
                    }
                }
            }

            if (modifiedRow == 0) sender.sendMessage(ChatColor.RED + "No commands to modify.");
            else sender.sendMessage("" + ChatColor.RED + modfied + ChatColor.GOLD + " command(s) in " + ChatColor.RED + modifiedRow + ChatColor.GOLD + " row(s) are modified");
            return true;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) return Collections.emptyList();

        final List<String> list = new ArrayList<>();
        final String cmdS = CommandUtil.getCommand(alias, args);

        if (cmdS.matches("/crescendo ")
                || cmdS.matches("/crescendo (-?(\\d)+ ){3}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockX());
            return list;
        }
        else if (cmdS.matches("/crescendo (-?(\\d)+ ){1}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockY());
            return list;
        }
        else if (cmdS.matches("/crescendo (-?(\\d)+ ){2}")
                || cmdS.matches("/crescendo (-?(\\d)+ ){4}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockZ());
            return list;
        }
        else if (cmdS.matches("/crescendo (-?(\\d)+ ){5}")){
            list.addAll(Arrays.stream(Direction.values()).map(Enum::toString).collect(Collectors.toList()));
            return list;
        }
        else if (cmdS.matches("/crescendo (-?(\\d)+ ){5}(SOUTH|NORTH|WEST|EAST) ")){
            list.add("0.0");
            return list;
        }
        else if (cmdS.matches("/crescendo (-?(\\d)+ ){5}(SOUTH|NORTH|WEST|EAST) ((\\d)+.(\\d)+) ")){
            list.add("1.0");
            return list;
        }
        else return Collections.emptyList();
    }

    private boolean isCorrect(String command){
        return command.matches("/crescendo (-?(\\d)+ ){5}(SOUTH|NORTH|WEST|EAST) ((\\d)+.(\\d)+) ((\\d)+.(\\d)+)");
    }

    private int calculateAnzCom(World world, Location startLoc, Location endLoc, Direction dir){
        int anzCom = 0;

        double x, y = startLoc.getBlockY(), z;

        if (dir == Direction.EAST || dir == Direction.WEST){
            for (x = startLoc.getBlockX(); (dir == Direction.WEST) ? x >= endLoc.getBlockX() : x <= endLoc.getBlockX(); x = (dir == Direction.WEST) ? x - 1 : x + 1){
                for (z = startLoc.getBlockZ(); (dir == Direction.WEST) ? z >= endLoc.getBlockZ() : z <= endLoc.getBlockZ(); z = (dir == Direction.WEST) ? z - 1 : z + 1){
                    if (world.getBlockAt((int)x, (int)y, (int)z).getState() instanceof CommandBlock){
                        anzCom++;
                        break;
                    }
                }
            }
        }
        else{
            for (z = startLoc.getBlockZ(); (dir == Direction.NORTH) ? z >= endLoc.getBlockZ() : z <= endLoc.getBlockZ(); z = (dir == Direction.NORTH) ? z - 1 : z + 1){
                for (x = startLoc.getBlockX(); (dir == Direction.NORTH) ? x >= endLoc.getBlockX() : x <= endLoc.getBlockX(); x = (dir == Direction.NORTH) ? x - 1 : x + 1){
                    if (world.getBlockAt((int)x, (int)y, (int)z).getState() instanceof CommandBlock){
                        anzCom++;
                        break;
                    }
                }
            }
        }

        return anzCom;
    }
}
