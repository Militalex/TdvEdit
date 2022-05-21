package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.TdvEdit;
import de.militaermiltz.tdv.events.ResourcePackListener;
import de.militaermiltz.tdv.events.ServerResourcePackListener;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author Alexander Ley
 * @version 1.0
 *
 * This command defines the resource pack command which shows
 * the link to external needed resource packs.
 *
 */
public class ResourcePackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender){
            sender.sendMessage("Only player can use this command.");
            return true;
        }

        int behave = TdvEdit.PLUGIN.getConfig().getInt("PackBehave");

        if (behave == 0){
            sender.sendMessage(ChatColor.RED + "There is no resourcepack message defined.");
        }
        else if (behave == 1){
            ServerResourcePackListener.showMessage((Player) sender);
        }
        else if (behave == 2){
            ResourcePackListener.showMessage((Player) sender);
        }

        return true;
    }
}
