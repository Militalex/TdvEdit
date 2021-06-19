package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.util.RegexUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This class defines the /commandprepend command
 *
 */
public class CommandPrependCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final String commandString = CommandUtil.getCommand(label, args);

        if (sender instanceof ConsoleCommandSender) sender.sendMessage("This command is only usable by players or command blocks.");
        if (!isComplete(commandString)) return false;

        //Combined String args
        final String prependString = getPrependString(commandString);
        final String filter = getFilter(commandString);

        final World world = CommandUtil.getWorldFromSender(sender);
        final Location[] fromTo = CommandUtil.transformLocation(new Location(world, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])),
                                                                new Location(world, Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));

        //Count how many cmd-Blocks are modified
        int modified = 0;

        for (int x = fromTo[0].getBlockX(); x <= fromTo[1].getBlockX(); x++) {
            for (int y = fromTo[0].getBlockY(); y <= fromTo[1].getBlockY(); y++) {
                for (int z = fromTo[0].getBlockZ(); z <= fromTo[1].getBlockZ(); z++) {
                    final Block block = world.getBlockAt(x, y, z);

                    if (block.getState() instanceof CommandBlock) {
                        //Get Command String
                        String cmd = ((CommandBlock) block.getState()).getCommand();

                        //Remove "/"
                        if (cmd.charAt(0) == '/') {
                            cmd = cmd.replaceFirst("/", "");
                        }

                        if (!filter.equals("")){
                            //Prepends the arg String without "
                            cmd = prependString.substring(1, prependString.length() - 1) + " " + cmd;

                            CommandUtil.setCMDinBlock(block, cmd);
                            modified++;
                        }
                        else {
                            if (cmd.split(" ")[0].equals(filter)) {
                                //Prepends the arg String without "
                                cmd = prependString.substring(1, prependString.length() - 1) + " " + cmd;

                                CommandUtil.setCMDinBlock(block, cmd);
                                modified++;
                            }
                        }

                        //Length is 8 when command filter is applied
                        if (args.length == 8) {
                            String argument = args[7];

                            //Remove "/"
                            if (argument.charAt(0) == '/') {
                                argument = argument.replaceFirst("/", "");
                            }

                            //If command equals filter
                            if (cmd.split(" ")[0].equals(argument)) {
                                //Prepends the arg String without "
                                cmd = args[6].substring(1, args[6].length() - 1) + " " + cmd;

                                CommandUtil.setCMDinBlock(block, cmd);
                                modified++;
                            }
                        }
                        else{
                            //Prepends the arg String without "
                            cmd = args[6].substring(1, args[6].length() - 1) + " " + cmd;

                            CommandUtil.setCMDinBlock(block, cmd);
                            modified++;
                        }
                    }
                }
            }
        }
        if (modified == 0) sender.sendMessage(ChatColor.RED + "No commands to modify.");
        else sender.sendMessage("" + ChatColor.RED + modified + ChatColor.GOLD + " command(s) are modified.");

        return true;
    }

    /**
     * Checks if all components of the command are available to execute.
     */
    private boolean isComplete(String command){
        if (command.matches("/commandprepend (-?(\\d)+ ){6}([\"][\\D|\\d]*[\"])")) return true;

       if (!command.matches("/commandprepend (-?(\\d)+ ){6}([\"][\\D|\\d]*[\"]) [\\D\\d]+")){
           return false;
       }
       return CommandUtil.COMMANDS.contains(command.replaceAll("/commandprepend (-?(\\d)+ ){6}([\"][\\D|\\d]*[\"]) [\\D\\d]+", ""));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) return Collections.emptyList();

        final List<String> list = new ArrayList<>();
        final String cmdS = CommandUtil.getCommand(alias, args);

        if (cmdS.matches("/commandprepend ")
                || cmdS.matches("/commandprepend (-?(\\d)+ ){3}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockX());
            return list;
        }
        else if (cmdS.matches("/commandprepend (-?(\\d)+ ){1}")
                || cmdS.matches("/commandprepend (-?(\\d)+ ){4}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockY());
            return list;
        }
        else if (cmdS.matches("/commandprepend (-?(\\d)+ ){2}")
                || cmdS.matches("/commandprepend (-?(\\d)+ ){5}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockZ());
            return list;
        }
        else if (cmdS.matches("/commandprepend (-?(\\d)+ ){6}")){
            list.add("\"\"");
            return list;
        }
        else if (cmdS.matches("/commandprepend (-?(\\d)+ ){6}([\"][\\D|\\d]*[\"]) ([^ |0-9]|[\\d])+")){
            list.addAll(CommandUtil.COMMANDS);
            return list;
        }
        else return Collections.emptyList();


        /*switch (args.length) {
            case 1:
            case 4:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockX() : 0));
                break;
            case 2:
            case 5:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockY() : 0));
                break;
            case 3:
            case 6:
                list.add("" + ((playerLookLocation != null) ? playerLookLocation.getBlockZ() : 0));
                break;
            case 7:
                list.add("\"\"");
                break;
            case 8:
                list.addAll(CommandUtil.COMMANDS);
                break;
        }
        return list;*/
    }

    /**
     * Combines the String args to one argument.
     */
    private String getPrependString(String command){
        return RegexUtil.getMatches("[\"][\\D|\\d]*[\"]", command).get(0);
    }

    private String getFilter(String command){
        return command
                .replaceAll("/commandprepend (-?(\\d)+ ){6}([\"][\\D|\\d]*[\"])\\s?", "")
                .replaceFirst("/", "");
    }
}
