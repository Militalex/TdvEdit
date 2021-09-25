package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.TdvEdit;
import de.tr7zw.nbtapi.NBTFile;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Alexander Ley
 * @version 1.0
 *
 * This class defines the /calibratemusic command
 */
public class CalibrationCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //isCorrect
        if (args.length != 4){
            sender.sendMessage(ChatColor.RED + "Command Arguments are not suitable.");
            return false;
        }
        //Command arguments
        final int x = Integer.parseInt(args[0]);
        final int y = Integer.parseInt(args[1]);
        final int z = Integer.parseInt(args[2]);
        final String musikPatternStart = args[3];

        //Structure Files
        final Path structureFolder = Paths.get(TdvEdit.propertiesManager.getProperty("level-name") + "/generated/minecraft/structures");
        final List<Path> structureFiles;

        try {
            structureFiles = Files.list(structureFolder).filter(path -> path.toFile().getName().startsWith(musikPatternStart)).collect(Collectors.toList());
        }
        catch (IOException e) {
            TdvEdit.PLUGIN.getLogger().log(Level.SEVERE, "An error occurred by locating structure Files.", e);
            sender.sendMessage(ChatColor.RED + "An error occurred by locating structure Files.");
            return true;
        }

        //Counter
        final AtomicInteger modified = new AtomicInteger();

        //NBTFile Creation
        final List<NBTFile> nbtFiles = structureFiles.stream().map(path -> {
            try {
                return new NBTFile(path.toFile());
            }
            catch (IOException e) {
                TdvEdit.PLUGIN.getLogger().log(Level.SEVERE, "An error occurred by loading structure Files.", e);
                sender.sendMessage(ChatColor.RED + "An error occurred by loading structure Files.");
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        //NBTFile manipulkation
        nbtFiles.stream().map(nbtFile -> nbtFile.getCompoundList("blocks"))
        .forEach(nbtListCompounds ->
                nbtListCompounds.stream()
                .filter(nbtListCompound -> nbtListCompound.hasKey("nbt"))
                .map(nbtListCompound -> nbtListCompound.getCompound("nbt"))
                .filter(nbtCompound -> nbtCompound.hasKey("Command"))
                .forEach(nbtCompound -> {
                    //Get Command String
                    final String prependString = "/execute positioned " + x + " " + y + " " + z + " run ";
                    String cmd = nbtCompound.getString("Command");

                    //Remove "/"
                    if (cmd.charAt(0) == '/') {
                        cmd = cmd.replaceFirst("/", "");
                    }

                    if (cmd.split(" ")[0].equals("playsound")) {
                        //Prepends the arg String without "
                        cmd = prependString.substring(1, prependString.length() - 1) + " " + cmd;

                        nbtCompound.setString("Command", cmd);
                    }
                })
        );

        //Save NBTFiles
        nbtFiles.forEach(nbtFile -> {
            try {
                nbtFile.save();
                modified.getAndIncrement();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (modified.get() == 0) sender.sendMessage(ChatColor.RED + "No structures to modify.");
        else sender.sendMessage("" + ChatColor.RED + modified.get() + ChatColor.GOLD + " structure(s) are modified.");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender) return Collections.emptyList();

        final List<String> list = new ArrayList<>();
        final String cmdS = CommandUtil.getCommand(alias, args);

        if (cmdS.matches("/calibratemusic ")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockX());
            return list;
        }
        else if (cmdS.matches("/calibratemusic (-?(\\d)+ ){1}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockY());
            return list;
        }
        else if (cmdS.matches("/calibratemusic (-?(\\d)+ ){2}")){
            final Location playerLookLocation = CommandUtil.getPlayerLookBlockPos((Player) sender);
            list.add(""+ playerLookLocation.getBlockZ());
            return list;
        }
        else return Collections.emptyList();
    }
}
