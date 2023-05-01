package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.util.Dynamic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Militalex
 * @version 1.1
 *
 * This class defines givenote command to give players ability to give predefines command- and noteblocks.
 *
 */
public class GiveNoteCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender){
            sender.sendMessage("Only player can use this command.");
            return true;
        }
        if (!isCorrect(CommandUtil.getCommand(label, args))){
            return false;
        }

        try {
            final int noteClick = CommandUtil.getClickFromNote(args[0]);

            if (noteClick > 24) {
                sender.sendMessage(ChatColor.RED + "" + noteClick + " is not valid for noteblocks/playsound.");
                return true;
            }

            if (args.length == 1 || args[1].equals("nb")){
                Bukkit.dispatchCommand(sender, "give @s note_block{BlockStateTag:{note:\"" + noteClick +"\"}}");
            }
            else{
                final String block = (args[1].equals("cb")) ? "command_block" : "repeating_command_block";
                final String instrument = CommandUtil.getInstrumentFromBlock(args[2]);
                final double volume = (args.length == 3) ? 1.0 : ((Dynamic.getStringValues().contains(args[3])) ? Dynamic.valueOf(args[3]).getVolume() : Double.parseDouble(args[3]));

                Bukkit.dispatchCommand(sender, "give @s " + block +
                        "{BlockEntityTag:{Command:\"/playsound minecraft:block.note_block." + instrument +
                        " master @a ~ ~ ~ " + volume + " " + CommandUtil.getPitchFromClicks(noteClick) + "\"}}");
            }
            return true;
        }
        catch (IllegalArgumentException exception){
            sender.sendMessage(ChatColor.RED + exception.getMessage());
            return true;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender){
            return Collections.emptyList();
        }

        if (args.length == 1){
            return Stream.of("F1#", "G1b", "G1", "G1#", "A1b", "A1", "A1#", "B1", "H1", "C1", "C1#", "D1b",
                             "D1", "D1#", "E1b", "F2", "F2#", "G2b", "G2", "G2#", "A2b", "A2", "A2#", "B2", "H2",
                             "C2", "C2#", "D2b", "D2", "D2#", "E2b", "E2", "F3", "F3#", "G3b",
                             "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                             "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"
            ).filter(s -> s.contains(args[0])).collect(Collectors.toList());
        }
        else if (args.length == 2){
            return Arrays.asList("nb", "cb", "rcb");
        }
        else if (args.length == 3 && (args[1].equals("cb") || args[1].equals("rcb"))){
            return Stream.of("Wood", "Sand", "Glass", "Stone", "Gold", "Clay", "PackedIce", "Wool",
                             "BoneBlock", "Iron", "SoulSand", "Pumpkin", "Emerald", "Hay", "Glowstone", "Air"
            ).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
        }
        else if (args.length == 4 && (args[1].equals("cb") || args[1].equals("rcb"))){
            final ArrayList<String> list = new ArrayList<>(Dynamic.getStringValues());
            list.add("1.0");
            return list;
        }
        else return Collections.emptyList();
    }

    private boolean isCorrect(String command){
        return command.matches("/givenote (([0-9]|(1[0-9])|(2[0-4]))|([A-H][1-2][#b]?)|(F3|F3#|G3b))( (nb|((cb|rcb) (Wood|Sand|Glass|Stone|Gold|Clay|PackedIce|Wool|BoneBlock|Iron|SoulSand|Pumpkin|Emerald|Hay|Glowstone|Air)( (([0-9]+.[0-9]+)|(ppp|pp|p|mp|mf|fff|ff|f)))?)))?");
    }
}
