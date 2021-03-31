package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.util.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //Combine String args
        args = fusionPrependArgs(args);

        if (!isComplete(args)) return false;
        if (sender instanceof ConsoleCommandSender) sender.sendMessage("This command is only usable by players or command blocks.");

        final World world = CommandUtil.getWorldFromSender(sender, args);
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

                                CommandUtil.setCommandInCommandBlock(block, cmd);
                                modified++;
                            }
                        }
                        else{
                            //Prepends the arg String without "
                            cmd = args[6].substring(1, args[6].length() - 1) + " " + cmd;

                            CommandUtil.setCommandInCommandBlock(block, cmd);
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
    private boolean isComplete(String[] args){
        if (args.length < 7 || args.length > 8) return false;

        for (int i = 0; i < args.length; i++){
            final String argument = args[i];

            switch (i){
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if (!NumberUtil.isInteger(argument)) return false;
                    break;
                case 6:
                    if (argument.charAt(0) != '\"' || argument.charAt(argument.length() - 1) != '\"') return false;
                    break;
                case 7:
                    if (!CommandUtil.COMMANDS.contains(argument)) return false;
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();

        Location playerLookLocation = null;

        if (args.length <= 6 && sender instanceof Player)
            playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);

        args = fusionPrependArgs(args);

        switch (args.length) {
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
        return list;
    }

    /**
     * Combines the String args to one argument.
     */
    private String[] fusionPrependArgs(String[] args){
        //Conditions to skip fusion
        if (args.length < 7 || args[6].equals("") || args[6].charAt(0) != '\"' || (args[6].charAt(0) == '\"' && args[6].charAt(args[6].length() - 1) == '\"')) return args;

        //Arglist
        final List<String> list = new ArrayList<>(Arrays.asList(args).subList(6, args.length));
        final StringBuilder builder = new StringBuilder("");


        for (int i = 0 ; i < list.size(); i++) {
            final String arg = list.get(i);

            //Append always first
            if (i == 0) {
                builder.append(arg);
            }
            else{
                builder.append(" ").append(arg);

                try{
                    //Check if next arg have to append and break if not
                    final String nextArg = list.get(i + 1);
                    if (CommandUtil.COMMANDS.contains(nextArg) || nextArg.equals("")) {
                        break;
                    }
                }
                catch (IndexOutOfBoundsException ignored){ }
            }
        }

        //Replace old arg to fusion arg
        args[6] = builder.toString();

        final List<String> argList = Arrays.stream(args).collect(Collectors.toList());

        //Remove args between 6 and end or filter arg
        for (int j = 7; j < argList.size() && !argList.get(j).equals("") && !CommandUtil.COMMANDS.contains(argList.get(j)); ){
            argList.remove(j);
        }

        return argList.toArray(new String[0]);
    }
}
