package de.militaermiltz.tdv.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            final int noteClick = getClickFromNote(args[0]);

            if (noteClick > 24) {
                sender.sendMessage(ChatColor.RED + "" + noteClick + " is not valid for noteblocks/playsound.");
                return true;
            }

            if (args.length == 1 || args[1].equals("nb")){
                Bukkit.getServer().dispatchCommand(sender, "give @s note_block{BlockStateTag:{note:\"" + noteClick +"\"}}");
            }
            else{
                final String block = (args[1].equals("cb")) ? "command_block" : "repeating_command_block";
                final String instrument = getInstrumentFromBlock(args[2]);
                final double volume = (args.length == 3) ? 1.0 : Double.parseDouble(args[3]);

                Bukkit.getServer().dispatchCommand(sender, "give @s " + block +
                        "{BlockEntityTag:{Command:\"/playsound minecraft:block.note_block." + instrument +
                        " master @a ~ ~ ~ " + volume + " " + getPitchFromClicks(noteClick) + "\"}}");
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
            return Collections.singletonList("1.0");
        }
        else return Collections.emptyList();
    }

    private boolean isCorrect(String command){
        return command.matches("/givenote (([0-9]|(1[0-9])|(2[0-4]))|([A-H][1-2][#b]?)|(F3|F3#|G3b))( (nb|((cb|rcb) (Wood|Sand|Glass|Stone|Gold|Clay|PackedIce|Wool|BoneBlock|Iron|SoulSand|Pumpkin|Emerald|Hay|Glowstone|Air)( ([0-9]+.[0-9]+))?)))?");
    }

    private double getPitchFromClicks(int clicks){
        return switch (clicks){
          case 0 -> 0.5;
          case 1 -> 0.53;
          case 2 -> 0.56;
          case 3 -> 0.6;
          case 4 -> 0.63;
          case 5 -> 0.67;
          case 6 -> 0.7;
          case 7 -> 0.75;
          case 8 -> 0.8;
          case 9 -> 0.85;
          case 10 -> 0.9;
          case 11 -> 0.95;
          case 12 -> 1.0;
          case 13 -> 1.05;
          case 14 -> 1.1;
          case 15 -> 1.2;
          case 16 -> 1.25;
          case 17 -> 1.32;
          case 18 -> 1.4;
          case 19 -> 1.5;
          case 20 -> 1.6;
          case 21 -> 1.7;
          case 22 -> 1.8;
          case 23 -> 1.9;
          case 24 -> 2.0;
          default -> throw new IllegalArgumentException(clicks + " is not valid for noteblocks/playsound.");
        };
    }

    private String getInstrumentFromBlock(String block){
        return switch (block){
          case "Wood" -> "bass";
          case "Sand" -> "snare";
          case "Glass" -> "hat";
          case "Stone" -> "basedrum";
          case "Gold" -> "bell";
          case "Clay" -> "flute";
          case "PackedIce" -> "chime";
          case "Wool" -> "guitar";
          case "BoneBlock" -> "xylophone";
          case "Iron" -> "iron_xylophone";
          case "SoulSand" -> "cow_bell";
          case "Pumpkin" -> "didgeridoo";
          case "Emerald" -> "bit";
          case "Hay" -> "banjo";
          case "Glowstone" -> "pling";
          case "Air" -> "harp";
          default -> throw new IllegalArgumentException(block + " is not a valid instrument.");
        };
    }

    private int getStepFromInterval(String step){
        return switch (step){
            case "note" -> 0;
            case "next" -> 1;
            case "second" -> 2;
            case "third" -> 3;
            case "fourth" -> 4;
            case "fifth" -> 5;
            case "sixth" -> 6;
            case "seventh" -> 7;
            case "octave" -> 8;
            default -> throw new IllegalArgumentException(step + " is not valid");
        };
    }

    private int getClickFromNote(String note){
        return switch (note) {
            case "F1#", "G1b", "0" -> 0;
            case "G1", "1" -> 1;
            case "G1#", "A1b", "2" -> 2;
            case "A1", "3" -> 3;
            case "A1#", "B1", "4" -> 4;
            case "H1", "5" -> 5;
            case "C1", "6" -> 6;
            case "C1#", "D1b", "7" -> 7;
            case "D1", "8" -> 8;
            case "D1#", "E1b", "9" -> 9;
            case "E1", "10" -> 10;
            case "F2", "11" -> 11;
            case "F2#", "G2b", "12" -> 12;
            case "G2", "13" -> 13;
            case "G2#", "A2b", "14" -> 14;
            case "A2", "15" -> 15;
            case "A2#", "B2", "16" -> 16;
            case "H2", "17" -> 17;
            case "C2", "18" -> 18;
            case "C2#", "D2b", "19" -> 19;
            case "D2", "20" -> 20;
            case "D2#", "E2b", "21" -> 21;
            case "E2", "22" -> 22;
            case "F3", "23" -> 23;
            case "F3#", "G3b", "24" -> 24;
            default -> throw new IllegalArgumentException(note + " is not valid.");
        };
    }
}
