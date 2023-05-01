package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.commands.util.ShowPlaysoundTickable;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Militalex
 * @version 1.0
 *
 * This class defines /showlaysoundstats command
 *
 */
public class ShowPlaysoundCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        // can only executed by players
        if (!(commandSender instanceof Player player)){
            commandSender.sendMessage(ChatColor.RED + "Only players can executes this command!");
            return true;
        }

        final ShowPlaysoundTickable tickable = ShowPlaysoundTickable.getInstance();
        if (tickable.containsPlayer(player)){
            tickable.removePlayer(player);
            commandSender.sendMessage(ChatColor.GOLD + "Playsound visualization: OFF");
        }
        else {
            tickable.addPlayer(player);
            commandSender.sendMessage(ChatColor.GREEN + "Playsound visualization: ON");
        }

        return true;
    }
}
