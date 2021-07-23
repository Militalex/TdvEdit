package de.militaermiltz.tdv.commands;

import de.militaermiltz.tdv.events.ResourcePackListener;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ResourcePackCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof ConsoleCommandSender || sender instanceof BlockCommandSender){
            sender.sendMessage("Only player can use this command.");
            return true;
        }

        ResourcePackListener.showMessage((Player) sender);
        return true;
    }
}
